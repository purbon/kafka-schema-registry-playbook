package com.purbon.kafka.clients;

import org.apache.avro.generic.GenericRecord;

public class AvroRunner {

  public static void main(String[] args) throws Exception {

    Transaction tx = new Transaction();
    GenericRecord record = SchemaBuilder.buildRecord(tx);
    System.out.println(record);

  }
}
