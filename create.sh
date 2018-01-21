#!/usr/bin/env bash
mysql -h localhost -u${MYSQL_PASSWORD} -p${MYSQL_PASSWORD} < ./src/main/resources/data/create.sql