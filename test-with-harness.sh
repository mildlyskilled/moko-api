#!/usr/bin/env bash
echo "Composing MySQL docker"
docker-compose -f docker-mysql up -d
sleep 5
sbt test
sbt it:test
echo "Destroying docker container"
docker stop $(docker ps -a -q --filter "name=moko*")
docker container prune -f