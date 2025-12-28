#!/usr/bin/env bash

set -e  # Exit immediately if any command fails

echo "                                      "
echo "======================================"
echo " Building application JAR"
echo "======================================"
./gradlew clean bootJar

echo "                                      "
echo "======================================"
echo " Starting services with Docker Compose"
echo "======================================"
docker compose up
