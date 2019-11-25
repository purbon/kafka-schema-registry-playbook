#!/usr/bin/env bash

# delete all connectors
docker-compose exec connect-debezium /scripts/delete-all-connectors.sh
