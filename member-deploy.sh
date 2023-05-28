#!/bin/sh
imgname=member-app
version=0.0.1-SNAPSHOT

mvn package -DskipTests
docker build -f ./member/Dockerfile -t nantaaditya/${imgname}:${version} .
docker run -d -p 8000:8000 --name ${imgname} nantaaditya/${imgname}:${version}