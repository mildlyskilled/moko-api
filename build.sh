#!/usr/bin/env bash
docker build -t mokoapi .
docker run -d -p 8080:8080 mokoapi