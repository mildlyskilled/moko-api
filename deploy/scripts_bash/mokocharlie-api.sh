#!/usr/bin/env bash

COMMIT_ID=$1
JVM_PARAMETERS=$2

#this one has to be manually overwritten
JAR_FILE=mokocharlie-api-$COMMIT_ID.jar

nohup java $JVM_PARAMETERS -jar $JAR_FILE > /dev/null 2>&1 &

#Sleep a couple of seconds, otherwise the process won't start before ansible disconnects
sleep 5
