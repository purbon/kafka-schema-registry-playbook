package com.purbon.kafka.clients;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.Arrays;
import java.util.Properties;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class MyAvroConsumer {

  public static final String GENERIC_MODE  = "generic";
  public static final String PAYMENT_MODE  = "payment";

  public Properties configure(String kafkaServers) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-avro-consumer"+System.currentTimeMillis());


    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return props;
  }

  private Properties props;

  public Properties configure() {
    return props;
  }

  public MyAvroConsumer(String kafkaServers) {
    props = configure(kafkaServers);

  }

  public void close() {
  }

  public void consumeGenericRecordTopic(String topic) {
    final Consumer<String, GenericRecord> consumer = new KafkaConsumer<>(configure());
    consumer.subscribe(Arrays.asList(topic));

    try {
      while (true) {
        ConsumerRecords<String, GenericRecord> records = consumer.poll(100);
        for (ConsumerRecord<String, GenericRecord> record : records) {
          record.headers().forEach(header -> {
            System.out.println(header.key() + " " + header.value());
          });
          System.out.printf("offset = %d, key = %s, value = %s \n", record.offset(), record.key(),
              record.value());
        }
      }
    }  finally {
      consumer.close();
    }
  }

  public void consumePayment(String topic) {
    final Consumer<String, Payment> consumer = new KafkaConsumer<>(configure());
    consumer.subscribe(Arrays.asList(topic));

    try {
      while (true) {
        ConsumerRecords<String, Payment> records = consumer.poll(100);
        System.out.println("READ-RECORDS: "+records.count());
        for (ConsumerRecord<String, Payment> record : records) {
          String key = record.key();
          Payment value = record.value();
          System.out.println(key+" "+value);
        }
      }
    }  finally {
      consumer.close();
    }
  }


  public static void main(String [] args) {

    String kafkaServers = "localhost:9092";
    String runMode = (args.length > 0 && args[0] != null) ? args[0] : GENERIC_MODE;

    MyAvroConsumer avroConsumer = new MyAvroConsumer(kafkaServers);

    switch (runMode) {
      case GENERIC_MODE:
        final String genericTopic = "transactions";
        avroConsumer.consumeGenericRecordTopic(genericTopic);
        break;
      case PAYMENT_MODE:
        final String paymentsTopic = "transactions";
        avroConsumer.consumeGenericRecordTopic(paymentsTopic);
        break;

    }





  }
}
