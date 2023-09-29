## How to run project
- build member image
```shell
#!/bin/sh
IMAGE_NAME=member-app
IMAGE_VERSION=0.0.1-SNAPSHOT

mvn package -DskipTests
docker build -f ./member/Dockerfile -t nantaaditya/${IMAGE_NAME}:${IMAGE_VERSION} --build-arg IMAGE_NAME=${IMAGE_NAME} --build-arg IMAGE_VERSION=${IMAGE_VERSION} .
```

- build core image
```shell
#!/bin/sh
IMAGE_NAME=core-app
IMAGE_VERSION=0.0.1-SNAPSHOT

mvn package -DskipTests
docker build -f ./core/Dockerfile -t nantaaditya/${IMAGE_NAME}:${IMAGE_VERSION} --build-arg IMAGE_NAME=${IMAGE_NAME} --build-arg IMAGE_VERSION=${IMAGE_VERSION} .
```

- run infrastructure & microservices on docker
```shell
# build member image
# build core image
docker compose up -d

# pgadmin credentials
user: user@mail.com
password: password

# postgresql credentials
user: user
password: password
```