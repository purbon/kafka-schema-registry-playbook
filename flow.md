# Flow

* Produce simple message into Kafka with Kafka Avro Serializer and Schema Registry
* Consume simple message from Apache Kafka

(basic consumer)
docker-compose exec kafka kafka-console-consumer --bootstrap-server kafka:9092 --from-beginning  --topic topic-avro

docker-compose exec kafka kafka-console-consumer --bootstrap-server kafka:9092 --from-beginning  --topic topic-complex-avro

docker-compose exec kafka  kafka-avro-console-consumer --topic topic-complex-avro \
  --bootstrap-server kafka:9092 \
  --property print.key=true  --from-beginning

```bash
➜  docker-server docker-compose exec kafka kafka-run-class kafka.tools.DumpLogSegments --files /var/lib/kafka/data/topic-avro-0/00000000000000000000.log --print-data-log
Dumping /var/lib/kafka/data/topic-avro-0/00000000000000000000.log
Starting offset: 0
baseOffset: 0 lastOffset: 1 count: 2 baseSequence: -1 lastSequence: -1 producerId: -1 producerEpoch: -1 partitionLeaderEpoch: 0 isTransactional: false isControl: false position: 0 CreateTime: 1573654403799 size: 119 magic: 2 compresscodec: NONE crc: 163624536 isvalid: true
| offset: 0 CreateTime: 1573654403789 keysize: 14 valuesize: 8 sequence: -1 headerKeys: [] key: message1 payload: m1
| offset: 1 CreateTime: 1573654403799 keysize: 14 valuesize: 8 sequence: -1 headerKeys: [] key: message2 payload: m2
```

BACKWARDS compatibility example:

1.- Show backwards compatibility changes
* adding a new optional field
* removing a field (not implemented) is as well a backwards compatibility change.
2.- Show a forwards incompatible change is now allowed.
* adding a required field.
* run code.

show this error:

```bash
Exception in thread "main" org.apache.kafka.common.errors.SerializationException: Error registering Avro schema: {"type":"record","name":"NetworkEvent","namespace":"com.purbon.kafka.events","fields":[{"name":"hostName","type":"string"},{"name":"ipAddress","type":"string"},{"name":"latency","type":"long"},{"name":"origin","type":["null","string"],"default":null},{"name":"timestamp","type":"int"}]}
Caused by: io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException: Schema being registered is incompatible with an earlier schema; error code: 409
	at io.confluent.kafka.schemaregistry.client.rest.RestService.sendHttpRequest(RestService.java:230)
	at io.confluent.kafka.schemaregistry.client.rest.RestService.httpRequest(RestService.java:256)
```

how to test compatibility

```bash
$ curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
    http://localhost:8081/compatibility/subjects/Kafka-value/versions/latest
  {"is_compatible":true}
```

# Update compatibility requirements globally
$ curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "NONE"}' \
    http://localhost:8081/config
  {"compatibility":"NONE"}

# Update compatibility requirements under the subject "Kafka-value"
$ curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "FORWARD"}' \
    http://localhost:8081/config/topic-complex-avro-value

* Show schema compatibility with Payments code.


## IMPORTANT flow.

1.- Show production/consuption example. No change.

2.-
* Stop all
* Add extra field (timestamp, without default value)
* Show backwards compatibility will kick in with error (show error)
* Add default value to timestamp.
* Show flow works from end to end, timestamp is ignored in consumer.

3.-
* Stop all
* Remove amount field.
* Show no problem with backwards compatibility
* Show production.
* Update consumer.
* Show end to end.

4.- Show generic record consumption.
* Need to disable specific record class, otherwise it try to find the Payment class locally.
