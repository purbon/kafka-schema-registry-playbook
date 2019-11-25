package com.purbon.kafka.clients;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

public class MyAvroProducer {

  public static final String PLAINTEXT_MODE = "plaintext";
  public static final String GENERATED_MODE = "generated";
  public static final String BACKWARDS_MODE = "backwards";
  public static final String FORWARDS_MODE  = "forwards";
  public static final String PAYMENTS_MODE  = "payments";



  private Properties configure(String kafkaServers) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "my-producer");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    props.put("schema.registry.url", "http://localhost:8081");
    return props;
  }

  private Properties props;
  private KafkaProducer<Object, Object> producer;

  public MyAvroProducer(String kafkaServers) {
    props = configure(kafkaServers);
    producer = new KafkaProducer<>(props);

  }

  public void send(String topic, String key, String value) {

    String userSchema = "{ \"type\":\"record\"," +
                           "\"name\":\"myrecord\"," +
                           "\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";

    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(userSchema);
    GenericRecord avroRecord = new GenericData.Record(schema);
    avroRecord.put("f1", value);

    ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, key, avroRecord);

    producer.send(record);
  }

  public void sendWithSchemaV0(String topic, String key, Map<String, Object> map) {
    Schema schema = SchemaBuilder.event();
    sendWithSchema(schema, topic, key, map);
  }
  public void sendWithSchemaV1_NewField(String topic, String key, Map<String, Object> map) {
    Schema schema = SchemaBuilder.eventWithNewField();
    sendWithSchema(schema, topic, key, map);
  }

  public void sendWithSchemaV2_AddField(String topic, String key, Map<String, Object> map) {
    Schema schema = SchemaBuilder.eventWithAddField();
    sendWithSchema(schema, topic, key, map);
  }

  public void sendWithSchemaV3_RemovedField(String topic, String key, Map<String, Object> map) {
    Schema schema = SchemaBuilder.eventWithRemovedField();
    sendWithSchema(schema, topic, key, map);
  }

  private void sendWithSchema(Schema schema, String topic, String key, Map<String, Object> map) {
    GenericRecord record = SchemaBuilder.buildRecord(schema, map);
    ProducerRecord<Object, Object> kafkaRecord = new ProducerRecord<>(topic, key, record);
    producer.send(kafkaRecord);
  }

  public void sendPayment(String topic, int total) throws InterruptedException {
    System.out.println("Sending "+total+" payments into "+topic);
    for (long i = 0; i < total; i++) {
      final String orderId = "id" + Long.toString(i);
      final Payment payment = new Payment(orderId, 100.00d, System.currentTimeMillis());
      final ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, payment.getId().toString(), payment);
      producer.send(record);
      Thread.sleep(1000);
    }
    System.out.println(total+" Payments send");
    close();
  }

  public void close() {
    producer.flush();
    producer.close();
  }

  public void runPlaintextSchema() {
    send("topic-avro", "message1", "m1");
    send("topic-avro", "message2", "m2");
  }

  public void runGeneratedSchema() {
    Map<String, Object> event = new HashMap<>();
    event.put("hostName", "foo");
    event.put("ipAddress", "1.1.1.1");
    event.put("latency", new Long(3));

    sendWithSchemaV0("topic-complex-avro", "message3", event);

    Map<String, Object> event2 = new HashMap<>();
    event2.put("hostName", "bar");
    event2.put("ipAddress", "0.0.0.0");
    event2.put("latency", new Long(10));

    sendWithSchemaV0("topic-complex-avro", "message4", event2);
  }

  public void runBackwardsCompatible() {

    Map<String, Object> event = new HashMap<>();
    event.put("hostName", "zet");
    event.put("ipAddress", "8.8.8.8");
    event.put("latency", new Long(30));
    event.put("origin", "dns-machine");

    sendWithSchemaV1_NewField("topic-complex-avro", "message5", event);
  }

  public void runForwardsCompatible() {

    Map<String, Object> event = new HashMap<>();
    event.put("hostName", "zetForwards");
    event.put("ipAddress", "81.81.81.81");
    event.put("origin", "forwards-dns-machine");
    event.put("latency", new Long(30));
    event.put("timestamp", 12344);

    sendWithSchemaV2_AddField("topic-complex-avro", "message6", event);
  }

  public static void main(String [] args) throws IOException, InterruptedException {

    String kafkaServers = "localhost:9092";
    String runMode = (args.length > 0 && args[0] != null) ? args[0] : PLAINTEXT_MODE;

    MyAvroProducer producer = new MyAvroProducer(kafkaServers);
    try {

      switch (runMode) {

        case PLAINTEXT_MODE:
          producer.runPlaintextSchema();
          break;
        case GENERATED_MODE:
          producer.runGeneratedSchema();
          break;
        case BACKWARDS_MODE:
          producer.runBackwardsCompatible();
          break;
        case FORWARDS_MODE:
          producer.runForwardsCompatible();
          break;
        case PAYMENTS_MODE:
          producer.sendPayment("transactions", 20);
          break;
        default:
          throw new IOException("Incorrect run option");
      }

    }
    finally {
      producer.close();
    }
  }
}
