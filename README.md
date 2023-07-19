## How to run project
- run docker compose to start `postgresql`, `pgadmin`, and `redis` instance
```shell
docker compose up -d
```
-  postgresql credentials
```text
user: user
password: password
```
- pgadmin credentials
```text
user: user@mail.com
password: password
```

- create db local_db
```sql
CREATE DATABASE local_db;
```
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
```