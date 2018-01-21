#!/usr/bin/env bash
mysql -h localhost -u${MYSQL_USER} -p${MYSQL_PASSWORD} < ./src/main/resources/data/create.sql