## How to run project
- build member image
```shell
#!/bin/sh
imgname=member-app
version=0.0.1-SNAPSHOT

mvn package -DskipTests
docker build -f ./member/Dockerfile -t nantaaditya/${imgname}:${version} .
```

- build core image
```shell
#!/bin/sh
imgname=core-app
version=0.0.1-SNAPSHOT

mvn package -DskipTests
docker build -f ./core/Dockerfile -t nantaaditya/${imgname}:${version} .
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