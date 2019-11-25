package com.purbon.kafka.clients;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.reflect.ReflectData;

public class SchemaBuilder {

  public static Schema eventWithNewField() {
    Schema networkEventsSchema = org.apache.avro.SchemaBuilder.record("NetworkEvent")
        .namespace("com.purbon.kafka.events")
        .fields()
        .requiredString("hostName")
        .requiredString("ipAddress")
        .requiredLong("latency")
        .optionalString("origin")
        .endRecord();
    return networkEventsSchema;
  }

  public static Schema eventWithRemovedField() {
    Schema networkEventsSchema = org.apache.avro.SchemaBuilder.record("NetworkEvent")
        .namespace("com.purbon.kafka.events")
        .fields()
        .requiredString("hostName")
        .requiredString("ipAddress")
        .optionalString("origin")
        .endRecord();
    return networkEventsSchema;
  }

  public static Schema eventWithAddField() {
    Schema networkEventsSchema = org.apache.avro.SchemaBuilder.record("NetworkEvent")
        .namespace("com.purbon.kafka.events")
        .fields()
        .requiredString("hostName")
        .requiredString("ipAddress")
        .requiredLong("latency")
        .optionalString("origin")
        .requiredInt("timestamp")
        .endRecord();
    return networkEventsSchema;
  }

  public static Schema event() {
    Schema networkEventsSchema = org.apache.avro.SchemaBuilder.record("NetworkEvent")
        .namespace("com.purbon.kafka.events")
        .fields()
        .requiredString("hostName")
        .requiredString("ipAddress")
        .requiredLong("latency")
        .endRecord();
    return networkEventsSchema;
  }

  public static GenericRecord buildRecord(Schema schema,  Map<String, Object> map) {
    GenericRecord record = new GenericData.Record(schema);
    map.forEach((key, value) -> record.put(key, value));
    return record;
  }

  public static GenericRecord buildRecord(Transaction tx) {
    final Schema schema = ReflectData.get().getSchema(tx.getClass());
    System.out.println("Schema");
    System.out.println(schema);

    final GenericData.Record record = new GenericData.Record(schema);

    schema.getFields().forEach(field -> {
      Object value = null;
      try {
        value = new PropertyDescriptor(field.name(), Transaction.class).getReadMethod().invoke(tx);
      } catch (Exception e) {
        e.printStackTrace();
      }
      record.put(field.name(), value);
    });

    return record;

  }

}
