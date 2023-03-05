## How to run project
- run docker compose to start postgresql and pgadmin instance
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
- run mvn clean install
```shell
mvn clean install
```
- run microservices
```shell
# member microservice
cd /member
mvn spring-boot:run

# core microservice
cd /core
mvn spring-boot:run
```