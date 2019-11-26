curl http://localhost:8083/connectors/mysql-source-demo-customers/status

#ä Restart connector
curl -X POST http://localhost:8083/connectors/mysql-source-demo-customers/restart
curl -X POST http://localhost:8083/connectors/mysql-source-demo-customers/tasks/0/restart
