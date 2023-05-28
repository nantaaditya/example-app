#!/bin/sh
imgname=core-app
version=0.0.1-SNAPSHOT

mvn package -DskipTests
docker build -f ./core/Dockerfile -t nantaaditya/${imgname}:${version} .
docker run -d -p 8001:8001 --name ${imgname} nantaaditya/${imgname}:${version}