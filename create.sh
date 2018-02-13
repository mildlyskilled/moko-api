#!/usr/bin/env bash
mysql -h localhost -u${MYSQL_USER} -p${MYSQL_PASSWORD} < ./src/main/resources/data/01.sql
mysql -h localhost -u${MYSQL_USER} -p${MYSQL_PASSWORD} < ./src/main/resources/data/02.sql
mysql -h localhost -u${MYSQL_USER} -p${MYSQL_PASSWORD} < ./src/main/resources/data/03.sql