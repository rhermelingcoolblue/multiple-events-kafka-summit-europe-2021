package io.confluent.developer.clients;

import io.confluent.developer.avro.CustomerEvent;
import io.confluent.developer.avro.PageView;
import io.confluent.developer.avro.Purchase;
import io.confluent.developer.utils.Data;
import io.confluent.developer.utils.PropertiesLoader;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializerConfig;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProducer {


    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Must provide path to properties file for configurations");
            System.exit(1);
        }
        var producerProperties = PropertiesLoader.load(args[0]);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var producerConfigs = new HashMap<String, Object>();
        producerProperties.forEach((k, v) -> producerConfigs.put((String) k, v));

        System.out.println("Producing records to topic with top-level Avro objects");
        produceAvroWrapped(producerConfigs);
        System.out.println("Producing records to topic with multi-Avro objects");
        produceAvro(producerConfigs);
        System.out.println("Producing records to JSON Schema multi-event topic");
        produceJsonSchema(producerConfigs);

    }

    private static void produceAvroWrapped(final Map<String, Object> originalConfigs) {
        Map<String, Object> producerConfigs = new HashMap<>(originalConfigs);
        producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        try (final Producer<String, CustomerEvent> producer = new KafkaProducer<>(producerConfigs)) {
            String topic = (String) producerConfigs.get("avro.wrapped.topic");
            List<CustomerEvent> events = new ArrayList<>();
            Purchase purchase = Data.avroPurchase();
            PageView pageView = Data.avroPageView();

            CustomerEvent.Builder builder = CustomerEvent.newBuilder();
            CustomerEvent eventOne = builder.setAction(purchase).setId(purchase.getCustomerId()).build();
            events.add(eventOne);
            CustomerEvent eventTwo = builder.setAction(pageView).setId(pageView.getCustomerId()).build();
            events.add(eventTwo);
            events.forEach(event -> producer.send(new ProducerRecord<>(topic, event.getId(), event), ((metadata, exception) -> {
                if (exception != null) {
                    System.err.printf("Producing %s resulted in error %s", event, exception);
                }
            })));
        }
    }

    private static void produceAvro(final Map<String, Object> originalConfigs) {
        Map<String, Object> producerConfigs = new HashMap<>(originalConfigs);
        producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerConfigs.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, false);
        producerConfigs.put(KafkaAvroSerializerConfig.USE_LATEST_VERSION, true);

        try (final Producer<String, SpecificRecordBase> producer = new KafkaProducer<>(producerConfigs)) {
            String topic = (String) producerConfigs.get("avro.topic");
            Purchase purchase = Data.avroPurchase();
            PageView pageView = Data.avroPageView();
            List<SpecificRecordBase> events = List.of(purchase, pageView);
            events.forEach(event -> producer.send(new ProducerRecord<>(topic, (String) event.get("customer_id"), event), ((metadata, exception) -> {
                if (exception != null) {
                    System.err.printf("Producing %s resulted in error %s", event, exception);
                }
            })));
        }
    }

    private static void produceJsonSchema(final Map<String, Object> originalConfigs) {
        Map<String, Object> producerConfigs = new HashMap<>(originalConfigs);
        producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSchemaSerializer.class);
        producerConfigs.put(KafkaJsonSchemaSerializerConfig.AUTO_REGISTER_SCHEMAS, false);
        producerConfigs.put(KafkaJsonSchemaSerializerConfig.USE_LATEST_VERSION, true);
        producerConfigs.put(KafkaJsonSchemaSerializerConfig.LATEST_COMPATIBILITY_STRICT, false);

        try (final Producer<String, Object> producer = new KafkaProducer<>(producerConfigs)) {
            String topic = (String) producerConfigs.get("json.topic");
            io.confluent.developer.json.Purchase purchase = Data.jsonSchemaPurchase();
            io.confluent.developer.json.PageView pageView = Data.jsonSchemaPageView();

            producer.send(new ProducerRecord<>(topic, purchase.getCustomerId(), purchase), ((metadata, exception) -> {
                if (exception != null) {
                    System.err.printf("Producing %s resulted in error %s", purchase, exception);
                }
            }));

            producer.send(new ProducerRecord<>(topic, pageView.getCustomerId(), pageView), ((metadata, exception) -> {
                if (exception != null) {
                    System.err.printf("Producing %s resulted in error %s", pageView, exception);
                }
            }));
        }
    }
}
