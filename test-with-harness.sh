#!/usr/bin/env bash
echo "Composing MySQL docker"
docker-compose -f docker-mysql up -d
sleep 5
sbt clean test
sleep 5
echo "Running integration tests"
sbt clean it:test
echo "Destroying docker container"
docker stop $(docker ps -a -q --filter "name=moko*")
docker container prune -f