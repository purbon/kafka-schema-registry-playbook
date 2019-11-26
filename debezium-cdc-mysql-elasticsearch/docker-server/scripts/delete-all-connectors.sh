#!/bin/sh

curl -XDELETE http://connect-debezium:8083/connectors/mysql-source-demo-invoices
curl -XDELETE http://connect-debezium:8083/connectors/mysql-source-demo-customers
curl -XDELETE http://connect-debezium:8083/connectors/mysql-source-demo-customers-raw
curl -XDELETE http://connect-debezium:8083/connectors/mysql-source-demo-invoices-raw

curl -XDELETE http://kafka-connect-cp:18083/connectors/elastic-sink
curl -XDELETE http://kafka-connect-cp:18083/connectors/elastic-sink-profiles
