package com.purbon.kafka.clients;

import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

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
}
