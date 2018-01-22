#!/usr/bin/env bash
mysql -h localhost -u${MYSQL_USER} -p${MYSQL_PASSWORD} -e "CREATE DATABASE mokocharlie;"
mysql -h localhost -u${MYSQL_USER} -p${MYSQL_PASSWORD} mokocharlie < ./src/main/resources/data/create.sql