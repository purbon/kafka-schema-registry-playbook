# Schema Registry for CDC usage example

## Preparing the environment

Start docker-compose:

```bash
$> docker-compose up -d
Creating docker-server_elasticsearch_1 ... done
Creating docker-server_mysql_1         ... done
Creating docker-server_zookeeper_1     ... done
Creating docker-server_kafka_1         ... done
Creating docker-server_kibana_1        ... done
Creating schema-registry               ... done
Creating docker-server_kafka-connect-cp_1 ... done
Creating docker-server_connect-debezium_1 ... done
```


Verify that docker is running as it should:
```bash
$> docker ps -a
CONTAINER ID        IMAGE                                                 COMMAND                  CREATED             STATUS                   PORTS                                                  NAMES
f023e40d06ba        confluentinc/cp-kafka-connect:5.3.1                   "/etc/confluent/dock…"   3 minutes ago       Up 3 minutes (healthy)   8083/tcp, 9092/tcp, 0.0.0.0:18083->18083/tcp           docker-server_kafka-connect-cp_1
d09892b90eee        debezium/connect:0.8                                  "/docker-entrypoint.…"   3 minutes ago       Up 3 minutes             8778/tcp, 9092/tcp, 0.0.0.0:8083->8083/tcp, 9779/tcp   docker-server_connect-debezium_1
b306a8287c95        confluentinc/cp-schema-registry:5.3.1                 "/etc/confluent/dock…"   3 minutes ago       Up 3 minutes             0.0.0.0:8081->8081/tcp                                 schema-registry
741d15ff494e        docker.elastic.co/kibana/kibana:6.3.0                 "/usr/local/bin/kiba…"   3 minutes ago       Up 3 minutes             0.0.0.0:5601->5601/tcp                                 docker-server_kibana_1
a81c4610c75b        confluentinc/cp-enterprise-kafka:5.3.1                "/etc/confluent/dock…"   3 minutes ago       Up 3 minutes             0.0.0.0:9092->9092/tcp                                 docker-server_kafka_1
ab8bff557bf1        docker.elastic.co/elasticsearch/elasticsearch:6.3.0   "/usr/local/bin/dock…"   3 minutes ago       Up 3 minutes             0.0.0.0:9200->9200/tcp, 9300/tcp                       docker-server_elasticsearch_1
0bf9f51c020e        confluentinc/cp-zookeeper:5.3.1                       "/etc/confluent/dock…"   3 minutes ago       Up 3 minutes             2181/tcp, 2888/tcp, 3888/tcp                           docker-server_zookeeper_1
825d336e61ab        debezium/example-mysql:0.8                            "docker-entrypoint.s…"   3 minutes ago       Up 3 minutes             0.0.0.0:3306->3306/tcp, 33060/tcp                      docker-server_mysql_1
```

## Schema Registry

In the initial phase no Schema is registered:

```bash
$> curl http://localhost:8081/subjects
[]%
```

However in the Debezium connector we've configured to use Schema Registry.
In the future each interaction of the data points (Schema) ingested using this Connector will be serialised and saved in the Schema Registry.

```bash
"key.converter":"io.confluent.connect.avro.AvroConverter",
"key.converter.schema.registry.url" : "http://schema-registry:8081",
"value.converter": "io.confluent.connect.avro.AvroConverter",
"value.converter.schema.registry.url" : "http://schema-registry:8081",
```

## Setting up the Connector (Debezium CDC)


```bash
$> docker-compose exec connect-debezium /scripts/create-mysql-source-avro.sh
HTTP/1.1 100 Continue

HTTP/1.1 201 Created
Date: Tue, 26 Nov 2019 09:09:23 GMT
Location: http://connect-debezium:8083/connectors/mysql-source-demo-customers
Content-Type: application/json
Content-Length: 1442
Server: Jetty(9.2.24.v20180105)

{"name":"mysql-source-demo-customers","config":{"connector.class":"io.debezium.connector.mysql.MySqlConnector","database.hostname":"mysql","database.port":"3306","database.user":"debezium","database.password":"dbz","database.server.id":"42","database.server.name":"asgard","table.whitelist":"demo.customers","database.history.kafka.bootstrap.servers":"kafka:29092","database.history.kafka.topic":"dbhistory.demo","include.schema.changes":"true","key.converter":"io.confluent.connect.avro.AvroConverter","key.converter.schema.registry.url":"http://schema-registry:8081","value.converter":"io.confluent.connect.avro.AvroConverter","value.converter.schema.registry.url":"http://schema-registry:8081","value.converter.schemas.enable":"false","internal.key.converter":"org.apache.kafka.connect.json.JsonConverter","internal.value.converter":"org.apache.kafka.connect.json.JsonConverter","transforms":"unwrap,InsertTopic,InsertSourceDetails","transforms.unwrap.type":"io.debezium.transforms.UnwrapFromEnvelope","transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value","transforms.InsertTopic.topic.field":"messagetopic","transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value","transforms.InsertSourceDetails.static.field":"messagesource","transforms.InsertSourceDetails.static.value":"Debezium CDC from MySQL on asgard","name":"mysql-source-demo-customers"},"tasks":[],"type":null}%                                          
```

Verify the connectors in the cluster:

```bash
$> curl http://localhost:8083/connectors
["mysql-source-demo-customers"]%
```

```bash
$>  curl http://localhost:8083/connectors/mysql-source-demo-customers/status
{"name":"mysql-source-demo-customers","connector":{"state":"RUNNING","worker_id":"192.168.112.9:8083"},"tasks":[{"state":"RUNNING","id":0,"worker_id":"192.168.112.9:8083"}],"type":"source"}%
```

## Verifying the data import process

The first step is to check the topics created in the Kafka Cluster:

```bash
$> docker-compose exec kafka kafka-topics --bootstrap-server kafka:9092 --list
__confluent.support.metrics
__consumer_offsets
_confluent-metrics
_schemas
asgard
asgard.demo.CUSTOMERS
connect-status
dbhistory.demo
docker-connect-debezium-configs
docker-connect-debezium-offsets
docker-kafka-connect-cp-configs
docker-kafka-connect-cp-offsets
docker-kafka-connect-cp-status
```

But what about the Schema Registry:

```bash
$> curl http://localhost:8081/subjects
["asgard-value","asgard.demo.CUSTOMERS-key","asgard-key","asgard.demo.CUSTOMERS-value"]%
```

Lets explore a given customers value schema:

```bash
$> curl http://localhost:8081/subjects/asgard.demo.CUSTOMERS-value/versions/latest | jq .
{
  "subject": "asgard.demo.CUSTOMERS-value",
  "version": 1,
  "id": 4,
  "schema": "{\"type\":\"record\",\"name\":\"Value\",\"namespace\":\"asgard.demo.CUSTOMERS\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"first_name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"last_name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"gender\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"club_status\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"comments\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"create_ts\",\"type\":{\"type\":\"string\",\"connect.version\":1,\"connect.default\":\"1970-01-01T00:00:00Z\",\"connect.name\":\"io.debezium.time.ZonedTimestamp\"},\"default\":\"1970-01-01T00:00:00Z\"},{\"name\":\"update_ts\",\"type\":{\"type\":\"string\",\"connect.version\":1,\"connect.default\":\"1970-01-01T00:00:00Z\",\"connect.name\":\"io.debezium.time.ZonedTimestamp\"},\"default\":\"1970-01-01T00:00:00Z\"},{\"name\":\"messagetopic\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"messagesource\",\"type\":[\"null\",\"string\"],\"default\":null}],\"connect.name\":\"asgard.demo.CUSTOMERS.Value\"}"
}
```

how does the table looks like:

```bash
describe CUSTOMERS;
+-------------+-------------+------+-----+-------------------+-----------------------------+
| Field       | Type        | Null | Key | Default           | Extra                       |
+-------------+-------------+------+-----+-------------------+-----------------------------+
| id          | int(11)     | NO   | PRI | NULL              |                             |
| first_name  | varchar(50) | YES  |     | NULL              |                             |
| last_name   | varchar(50) | YES  |     | NULL              |                             |
| email       | varchar(50) | YES  |     | NULL              |                             |
| gender      | varchar(50) | YES  |     | NULL              |                             |
| club_status | varchar(8)  | YES  |     | NULL              |                             |
| comments    | varchar(90) | YES  |     | NULL              |                             |
| create_ts   | timestamp   | NO   |     | CURRENT_TIMESTAMP |                             |
| update_ts   | timestamp   | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
+-------------+-------------+------+-----+-------------------+-----------------------------+
9 rows in set (0.00 sec)
```

## Updating the Table schema

### Verify the compatibility level

```bash
$> curl http://localhost:8081/config | jq .
{
  "compatibilityLevel": "BACKWARD"
}
```

curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"compatibility": "FULL"}' \
  http://localhost:8081/config/asgard.demo.CUSTOMERS-value

Set specific subject compatibility level.

```bash
$> curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{"compatibility": "FULL"}' \
  http://localhost:8081/config/asgard.demo.CUSTOMERS-value


{"compatibility":"FULL"}%
```

How verify this action:

```bash
$> curl http://localhost:8081/config/asgard.demo.CUSTOMERS-value | jq .
{
  "compatibilityLevel": "FULL"
}
```

### Doing changes in the table schema

Precondition: _"compatibilityLevel": "FULL"_


#### Add a new field

```sql
ALTER TABLE demo.CUSTOMERS ADD country varchar(100) NOT NULL AFTER gender;
```

```sql
insert into demo.CUSTOMERS
(`id`, `first_name`, `last_name`, `email`, `gender`, `country`, `club_status`, `comments`)
values
 (121219, 'Hans', 'Lindberg', 'hans@handball.de', 'Male', 'Dänemark', 'Weltstar', 'Füchse Berlin (2016 - 2021)');
```

What has happened?

```bash
curl http://localhost:8083/connectors/mysql-source-demo-customers/status | jq .
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  4288  100  4288    0     0   458k      0 --:--:-- --:--:-- --:--:--  465k
{
  "name": "mysql-source-demo-customers",
  "connector": {
    "state": "RUNNING",
    "worker_id": "192.168.112.9:8083"
  },
  "tasks": [
    {
      "state": "FAILED",
      "trace": "org.apache.kafka.connect.errors.DataException: asgard.demo.CUSTOMERS\n\tat io.confluent.connect.avro.AvroConverter.fromConnectData(AvroConverter.java:76)\n\tat org.apache.kafka.connect.runtime.WorkerSourceTask.sendRecords(WorkerSourceTask.java:228)\n\tat org.apache.kafka.connect.runtime.WorkerSourceTask.execute(WorkerSourceTask.java:194)\n\tat org.apache.kafka.connect.runtime.WorkerTask.doRun(WorkerTask.java:170)\n\tat org.apache.kafka.connect.runtime.WorkerTask.run(WorkerTask.java:214)\n\tat java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)\n\tat java.util.concurrent.FutureTask.run(FutureTask.java:266)\n\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)\n\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)\n\tat java.lang.Thread.run(Thread.java:748)\nCaused by: org.apache.kafka.common.errors.SerializationException: Error registering Avro schema: {\"type\":\"record\",\"name\":\"Value\",\"namespace\":\"asgard.demo.CUSTOMERS\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"first_name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"last_name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"gender\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"country\",\"type\":\"string\"},{\"name\":\"club_status\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"comments\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"create_ts\",\"type\":{\"type\":\"string\",\"connect.version\":1,\"connect.default\":\"1970-01-01T00:00:00Z\",\"connect.name\":\"io.debezium.time.ZonedTimestamp\"},\"default\":\"1970-01-01T00:00:00Z\"},{\"name\":\"update_ts\",\"type\":{\"type\":\"string\",\"connect.version\":1,\"connect.default\":\"1970-01-01T00:00:00Z\",\"connect.name\":\"io.debezium.time.ZonedTimestamp\"},\"default\":\"1970-01-01T00:00:00Z\"},{\"name\":\"messagetopic\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"messagesource\",\"type\":[\"null\",\"string\"],\"default\":null}],\"connect.name\":\"asgard.demo.CUSTOMERS.Value\"}\nCaused by: io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException: Schema being registered is incompatible with an earlier schema; error code: 409\n\tat io.confluent.kafka.schemaregistry.client.rest.RestService.sendHttpRequest(RestService.java:202)\n\tat io.confluent.kafka.schemaregistry.client.rest.RestService.httpRequest(RestService.java:229)\n\tat io.confluent.kafka.schemaregistry.client.rest.RestService.registerSchema(RestService.java:320)\n\tat io.confluent.kafka.schemaregistry.client.rest.RestService.registerSchema(RestService.java:312)\n\tat io.confluent.kafka.schemaregistry.client.rest.RestService.registerSchema(RestService.java:307)\n\tat io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient.registerAndGetId(CachedSchemaRegistryClient.java:115)\n\tat io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient.register(CachedSchemaRegistryClient.java:154)\n\tat io.confluent.kafka.serializers.AbstractKafkaAvroSerializer.serializeImpl(AbstractKafkaAvroSerializer.java:79)\n\tat io.confluent.connect.avro.AvroConverter$Serializer.serialize(AvroConverter.java:109)\n\tat io.confluent.connect.avro.AvroConverter.fromConnectData(AvroConverter.java:74)\n\tat org.apache.kafka.connect.runtime.WorkerSourceTask.sendRecords(WorkerSourceTask.java:228)\n\tat org.apache.kafka.connect.runtime.WorkerSourceTask.execute(WorkerSourceTask.java:194)\n\tat org.apache.kafka.connect.runtime.WorkerTask.doRun(WorkerTask.java:170)\n\tat org.apache.kafka.connect.runtime.WorkerTask.run(WorkerTask.java:214)\n\tat java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)\n\tat java.util.concurrent.FutureTask.run(FutureTask.java:266)\n\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)\n\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)\n\tat java.lang.Thread.run(Thread.java:748)\n",
      "id": 0,
      "worker_id": "192.168.112.9:8083"
    }
  ],
  "type": "source"
}
```

Recovering from this change:

```sql
ALTER TABLE demo.CUSTOMERS DROP COLUMN country;
```

```bash
$> curl -X POST http://localhost:8083/connectors/mysql-source-demo-customers/tasks/0/restart
$> curl http://localhost:8083/connectors/mysql-source-demo-customers/status | jq .
{
  "name": "mysql-source-demo-customers",
  "connector": {
    "state": "RUNNING",
    "worker_id": "192.168.112.9:8083"
  },
  "tasks": [
    {
      "state": "RUNNING",
      "id": 0,
      "worker_id": "192.168.112.9:8083"
    }
  ],
  "type": "source"
}
```

What about adding a FULL compatible change now:

```sql
ALTER TABLE demo.CUSTOMERS ADD country varchar(100)  NULL AFTER gender;

insert into demo.CUSTOMERS
(`id`, `first_name`, `last_name`, `email`, `gender`, `country`, `club_status`, `comments`)
values
 (121220, 'Marko', 'Kopljar', 'marko@handball.de', 'Male', 'Kroatien', 'gold', 'Erfahrene Füchse Berlin (2017 - 2020)');
 ```

 How are things going?

 ```bash
 curl http://localhost:8083/connectors/mysql-source-demo-customers/status | jq .
 {
   "name": "mysql-source-demo-customers",
   "connector": {
     "state": "RUNNING",
     "worker_id": "192.168.112.9:8083"
   },
   "tasks": [
     {
       "state": "RUNNING",
       "id": 0,
       "worker_id": "192.168.112.9:8083"
     }
   ],
   "type": "source"
 }```

```bash
curl http://localhost:8081/subjects/asgard.demo.CUSTOMERS-value/versions | jq .
[
  1,
  2
]
```

 ```bash
 curl http://localhost:8081/subjects/asgard.demo.CUSTOMERS-value/versions/latest | jq .
{
  "subject": "asgard.demo.CUSTOMERS-value",
  "version": 2,
  "id": 5,
  "schema": "{\"type\":\"record\",\"name\":\"Value\",\"namespace\":\"asgard.demo.CUSTOMERS\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"first_name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"last_name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"gender\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"country\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"club_status\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"comments\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"create_ts\",\"type\":{\"type\":\"string\",\"connect.version\":1,\"connect.default\":\"1970-01-01T00:00:00Z\",\"connect.name\":\"io.debezium.time.ZonedTimestamp\"},\"default\":\"1970-01-01T00:00:00Z\"},{\"name\":\"update_ts\",\"type\":{\"type\":\"string\",\"connect.version\":1,\"connect.default\":\"1970-01-01T00:00:00Z\",\"connect.name\":\"io.debezium.time.ZonedTimestamp\"},\"default\":\"1970-01-01T00:00:00Z\"},{\"name\":\"messagetopic\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"messagesource\",\"type\":[\"null\",\"string\"],\"default\":null}],\"connect.name\":\"asgard.demo.CUSTOMERS.Value\"}"
}
```

# Configurations option with Schema Registry and Kafka Connect

https://docs.confluent.io/current/schema-registry/connect.html#configuration-options

----
auto.register.schemas
Specify if the Serializer should attempt to register the Schema with Schema Registry

Type: boolean
Default: true
Importance: medium
----

----
max.schemas.per.subject
Maximum number of schemas to create or cache locally.

Type: int
Default: 1000
Importance: low
----

----
key.subject.name.strategy
Determines how to construct the subject name under which the key schema is registered with Schema Registry.

Any implementation of io.confluent.kafka.serializers.subject.SubjectNameStrategy can be specified. By default, <topic>-key is used as subject.

Type: class
Default: class io.confluent.kafka.serializers.subject.TopicNameStrategy
Importance: medium
value.subject.name.strategy
Determines how to construct the subject name under which the value schema is registered with Schema Registry.

Any implementation of io.confluent.kafka.serializers.subject.SubjectNameStrategy can be specified. By default, <topic>-value is used as subject.

Type: class
Default: class io.confluent.kafka.serializers.subject.TopicNameStrategy
Importance: medium
----
