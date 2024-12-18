package io.confluent.developer.streams;

import io.confluent.developer.avro.CustomerInfo;
import io.confluent.developer.avro.PageView;
import io.confluent.developer.avro.Purchase;
import io.confluent.developer.utils.PropertiesLoader;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MultiEventKafkaStreamsExample {
    public static void printMap(Map<?, ?> map) {
        System.out.println("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.println("    " + entry.getKey() + ": " + entry.getValue() + ",");
        }
        System.out.println("}");
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Must provide path to properties file for configurations");
            System.exit(1);
        }

        var streamsProperties = PropertiesLoader.load(args[0]);
        var mapConfigs = new HashMap<String, Object>();
        streamsProperties.forEach((k, v) -> mapConfigs.put((String) k, v));

        SpecificAvroSerde<CustomerInfo> customerSerde = getSpecificAvroSerde(mapConfigs);
        SpecificAvroSerde<SpecificRecord> specificAvroSerde = getSpecificAvroSerde(mapConfigs);

        String inputTopic = streamsProperties.getProperty("streams.input.topic.name");
        String outputTopic = streamsProperties.getProperty("streams.output.topic.name");

        StreamsBuilder builder = new StreamsBuilder();
        String storeName = "the_store";
        final StoreBuilder<KeyValueStore<String, CustomerInfo>> customerStore = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(storeName),
                Serdes.String(),
                customerSerde);

        streamsProperties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
        builder.addStateStore(customerStore);

        builder.stream(inputTopic, Consumed.with(Serdes.String(), specificAvroSerde)
                .withOffsetResetPolicy(Topology.AutoOffsetReset.EARLIEST))
                .processValues(new EventValueTransformerSupplier(storeName), storeName)
                .peek((k, v) -> System.out.printf("Customer info %s %n", v))
                .to(outputTopic, Produced.with(Serdes.String(), customerSerde));

        try (KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), streamsProperties)) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> kafkaStreams.close(Duration.ofSeconds(5))));
            kafkaStreams.start();
            countDownLatch.await();
        } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
        }
    }

    static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final Map<String, Object> configs) {
        final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();
        specificAvroSerde.configure(configs, false);
        return specificAvroSerde;
    }

    static class EventValueTransformerSupplier
            implements FixedKeyProcessorSupplier<String, SpecificRecord, CustomerInfo> {
        private final String storename;

        public EventValueTransformerSupplier(String storename) {
            this.storename = storename;
        }

        @Override
        public FixedKeyProcessor<String, SpecificRecord, CustomerInfo> get() {
            return new FixedKeyProcessor<>() {
                private KeyValueStore<String, CustomerInfo> store;
                private FixedKeyProcessorContext<String, CustomerInfo> context;

                @Override
                public void init(FixedKeyProcessorContext<String, CustomerInfo> context) {
                    store = context.getStateStore(storename);
                    this.context = context;
                }

                @Override
                public void process(FixedKeyRecord<String, SpecificRecord> fixedKeyRecord) {
                    String readOnlyKey = fixedKeyRecord.key();
                    SpecificRecord value = fixedKeyRecord.value();
                    CustomerInfo customerInfo = store.get(readOnlyKey);
                    if (customerInfo == null) {
                        customerInfo = CustomerInfo.newBuilder().setCustomerId(readOnlyKey).build();
                    }
                    if (value instanceof PageView) {
                        PageView pageView = (PageView) value;
                        customerInfo.getPageViews().add(pageView.getUrl());

                    } else if (value instanceof Purchase) {
                        Purchase purchase = (Purchase) value;
                        customerInfo.getItems().add(purchase.getItem());
                    }
                    store.put(readOnlyKey, customerInfo);
                    context.forward(fixedKeyRecord.withValue(customerInfo));
                }
            };
        }
    }
}
