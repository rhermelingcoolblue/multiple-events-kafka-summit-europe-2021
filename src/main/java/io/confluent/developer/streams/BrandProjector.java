package io.confluent.developer.streams;

import io.confluent.developer.avro.BrandAggregate;
import io.confluent.developer.avro.CustomerInfo;
import io.confluent.developer.avro.PageView;
import io.confluent.developer.avro.Purchase;
import io.confluent.developer.utils.PropertiesLoader;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class BrandProjector {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrandProjector.class);

    public static void printMap(Map<?, ?> map) {
        LOGGER.info("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            LOGGER.info("    " + entry.getKey() + ": " + entry.getValue() + ",");
        }
        LOGGER.info("}");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            LOGGER.info("Must provide path to properties file for configurations");
            System.exit(1);
        }
        LOGGER.info("Starting stream");

        var streamsProperties = PropertiesLoader.load(args[0]);
        var mapConfigs = new HashMap<String, Object>();
        streamsProperties.forEach((k, v) -> mapConfigs.put((String) k, v));

        streamsProperties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);

        final Serde<GenericRecord> valueGenericAvroSerde = new GenericAvroSerde();
        valueGenericAvroSerde.configure(mapConfigs, false); // `false` for record values

        SpecificAvroSerde<BrandAggregate> brandAggregateSerde = new SpecificAvroSerde<>();
        brandAggregateSerde.configure(mapConfigs, false);

        String inputTopic = "brands";
        String outputTopic = "brands-aggregate";

        String storeName = "brand_aggregate_store";
        final StoreBuilder<KeyValueStore<String, BrandAggregate>> brandAggregateStore = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(storeName),
                Serdes.String(),
                brandAggregateSerde);

        StreamsBuilder builder = new StreamsBuilder();
        builder.addStateStore(brandAggregateStore);

        builder.stream(inputTopic, Consumed.with(Serdes.String(), valueGenericAvroSerde)
                .withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .processValues(new EventValueTransformerSupplier(storeName), storeName)
                .peek((k, v) -> System.out.printf("Brand aggregate %s %n", v))
                .to(outputTopic, Produced.with(Serdes.String(), brandAggregateSerde));

        try (KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), streamsProperties)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> kafkaStreams.close(Duration.ofSeconds(5))));
            kafkaStreams.setStateListener((newState, oldState) -> {
                LOGGER.info(String.format("Kafka Streams state changed from %s to %s \n", oldState, newState));
            });
            kafkaStreams.start();
            countDownLatch.await();
        } catch (InterruptedException e) {
            valueGenericAvroSerde.close();
            Thread.currentThread().interrupt();
        }
    }

    static class EventValueTransformerSupplier
            implements FixedKeyProcessorSupplier<String, GenericRecord, BrandAggregate> {
        private final String storename;

        public EventValueTransformerSupplier(String storename) {
            this.storename = storename;
        }

        @Override
        public FixedKeyProcessor<String, GenericRecord, BrandAggregate> get() {
            return new FixedKeyProcessor<>() {
                private KeyValueStore<String, BrandAggregate> store;
                private FixedKeyProcessorContext<String, BrandAggregate> context;

                @Override
                public void init(FixedKeyProcessorContext<String, BrandAggregate> context) {
                    store = context.getStateStore(storename);
                    this.context = context;
                }

                @Override
                public void process(FixedKeyRecord<String, GenericRecord> fixedKeyRecord) {
                    String readOnlyKey = fixedKeyRecord.key();
                    GenericRecord value = fixedKeyRecord.value();
                    BrandAggregate brandAggregate = store.get(readOnlyKey);
                    Object parsedEvent = BrandEventParser.parse(value);

                    BrandAggregate newlyCreatedOrUpdatedBrandAggregate = this.updateOrCreateBrandAggregateFromEvent(
                            readOnlyKey,
                            brandAggregate,
                            parsedEvent);

                    if (newlyCreatedOrUpdatedBrandAggregate != null) {
                        store.put(readOnlyKey, newlyCreatedOrUpdatedBrandAggregate);
                        context.forward(fixedKeyRecord.withValue(newlyCreatedOrUpdatedBrandAggregate));
                    }
                }

                private BrandAggregate updateOrCreateBrandAggregateFromEvent(String aggregateId,
                        BrandAggregate brandAggregate,
                        Object parsedEvent) {
                    if (parsedEvent == null) {
                        return null;
                    }

                    if (parsedEvent instanceof BrandAddedEvent) {
                        BrandAddedEvent brandAddedEvent = (BrandAddedEvent) parsedEvent;
                        return BrandAggregate.newBuilder()
                                .setAggregateId(aggregateId)
                                .setInternalName(brandAddedEvent.internalName)
                                .build();
                    }

                    if (brandAggregate == null) {
                        LOGGER.info(String.format(
                                "Something went wrong here, unable to find BrandAggregate for aggregateId: %v and receiver other event than BrandAdded \n",
                                aggregateId));
                        return null;
                    }

                    if (parsedEvent instanceof BrandChangedEvent) {
                        BrandChangedEvent brandChangedEvent = (BrandChangedEvent) parsedEvent;
                        brandAggregate.setInternalName(brandChangedEvent.internalName);
                        return brandAggregate;
                    }

                    if (parsedEvent instanceof BrandContentAddedEvent) {
                        BrandContentAddedEvent brandContentAddedEvent = (BrandContentAddedEvent) parsedEvent;

                        Map<String, String> localizedName = brandAggregate.getLocalizedName();
                        if (localizedName == null) {
                            localizedName = new HashMap<>();
                            brandAggregate.setLocalizedName(localizedName);
                        }
                        localizedName.put(String.valueOf(brandContentAddedEvent.languageId),
                                brandContentAddedEvent.name);

                        brandAggregate.setPrimaryImageId((double) brandContentAddedEvent.imageId);

                        if (brandContentAddedEvent.description.isPresent()) {

                            Map<String, String> localizedDescription = brandAggregate.getLocalizedDescription();
                            if (localizedDescription == null) {
                                localizedDescription = new HashMap<>();
                                brandAggregate.setLocalizedDescription(localizedDescription);
                            }

                            localizedDescription.put(String.valueOf(brandContentAddedEvent.languageId),
                                    brandContentAddedEvent.description.get());
                        }

                        return brandAggregate;
                    }

                    return null;
                }

            };
        }
    }
}
