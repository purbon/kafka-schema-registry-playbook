#!/usr/bin/env bash

# create the mysql source connector for debezium
docker-compose exec connect-debezium /scripts/create-mysql-source.sh

# create the elasticsearch sink connector
docker-compose exec connect-debezium /scripts/create-elasticsearch-sink.sh
