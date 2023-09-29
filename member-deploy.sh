#!/bin/sh
IMAGE_NAME=member-app
IMAGE_VERSION=0.0.1-SNAPSHOT

mvn package -DskipTests
docker build -f ./core/Dockerfile -t nantaaditya/${IMAGE_NAME}:${IMAGE_VERSION} --build-arg IMAGE_NAME=${IMAGE_NAME} --build-arg IMAGE_VERSION=${IMAGE_VERSION} .
docker run -d -p 8001:8001 --name ${IMAGE_NAME} nantaaditya/${IMAGE_VERSION}:${IMAGE_VERSION}