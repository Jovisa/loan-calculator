#!/usr/bin/env bash

set -e  # Exit immediately if any command fails

echo "                                      "
echo "======================================"
echo " Cleaning up environment..."
echo "======================================"
docker compose down --remove-orphans
./gradlew --stop

echo "Checking Docker availability"
docker info

export TESTCONTAINERS_RYUK_DISABLED=true

echo "                                      "
echo "======================================"
echo " Running tests"
echo "======================================"
REDIS_HOST= REDIS_PORT= ./gradlew clean test --no-daemon

echo "                                      "
echo "======================================"
echo " Building application JAR"
echo "======================================"
./gradlew clean bootJar

echo "                                      "
echo "======================================"
echo " Starting services with Docker Compose"
echo "======================================"
docker compose up -d