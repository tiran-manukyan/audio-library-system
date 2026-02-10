# Audio Library System

A small, pragmatic microservices project built with **Spring Boot + Spring Data JPA + PostgreSQL**, split into two services:

- **resource-service**: stores MP3 bytes and extracts metadata (via Apache Tika).
- **song-service**: stores and serves song metadata.

The services communicate over HTTP, and the **resource-service uses an “outbox” pattern** to reliably deliver metadata changes to the song-service.

---

## Architecture (high level)

### Services

#### 1) `resource-service`
Responsibilities:
- Accept an MP3 upload and store the raw bytes in its DB.
- Extract metadata from the audio (name/artist/album/year/duration).
- Enqueue “create metadata” / “delete metadata” events into an **outbox table**.
- Publish outbox events to `song-service` in batches via scheduled jobs (and/or transaction events).

API (core):
- `POST /resources` (`Content-Type: audio/mpeg`) → stores file, returns generated resource ID
- `GET /resources/{id}` → returns MP3 bytes (`audio/mpeg`)
- `DELETE /resources?id=<csv>` → deletes resources by CSV list of IDs

#### 2) `song-service`
Responsibilities:
- Persist metadata records keyed by `id` (same ID as resource ID).
- Support single and bulk create.
- Support delete by CSV or bulk request body.

API (core):
- `POST /songs` → create one song metadata record
- `POST /songs/bulk` → create many records (idempotent/conflict tolerant)
- `GET /songs/{id}` → fetch metadata
- `DELETE /songs?id=<csv>` → delete by CSV list of IDs
- `POST /songs/delete-bulk` → delete in bulk via JSON body

---

## Data & communication flow

### Upload flow
1. Client uploads an MP3 to `resource-service`.
2. `resource-service` extracts metadata and stores MP3 bytes.
3. `resource-service` writes an outbox event (`CREATE_METADATA`) in the same transaction.
4. Outbox publisher delivers the event(s) to `song-service` (immediate/async + scheduled batch retries).

### Delete flow
1. Client requests delete on `resource-service` using a CSV list of resource IDs.
2. `resource-service` deletes rows and writes outbox events (`DELETE_METADATA`) for the successfully deleted IDs.
3. Outbox publisher calls `song-service` to delete metadata for those IDs.

This keeps the two services **eventually consistent** while remaining resilient to temporary failures of either service.

---

## Tech stack

- Java **21** (Maven modules)
- Spring Boot **3.5.x**
- Spring MVC, Spring Validation
- Spring Data JPA (Hibernate)
- PostgreSQL (two separate DBs)
- Lombok, MapStruct (in song-service)
- Apache Tika (in resource-service) for audio metadata extraction
- Docker Compose for local Postgres instances

---

## Local development setup

### Prerequisites
- Java 21
- Maven (or use the included Maven Wrapper)
- Docker + Docker Compose

### 1) Start databases
From the repository root:

```shell script
docker compose up -d
```


This starts:
- `resource-db` (database `resource-db`)
- `song-db` (database `song-db`)

> Note: both containers use default credentials (`postgres` / `postgres`) for local development.

### 2) Run services

#### Option A: run each service from its module
In two terminals:

**song-service**
```shell script
./mvnw -pl song-service spring-boot:run
```


**resource-service**
```shell script
./mvnw -pl resource-service spring-boot:run
```


#### Option B: build jars then run
```shell script
./mvnw clean package
java -jar song-service/target/song-service-*.jar
java -jar resource-service/target/resource-service-*.jar
```


### 3) Verify
- resource-service: http://localhost:8080
- song-service: http://localhost:8081

---

## Configuration

### resource-service
`resource-service/src/main/resources/application.properties` includes:
- DB: `jdbc:postgresql://localhost:5432/resource-db`
- Server port: `8080`
- Song service URL: `http://localhost:8081`
- Outbox settings:
    - max attempts
    - batch sizes
    - scheduler enablement + delays

### song-service
`song-service/src/main/resources/application.properties` includes:
- DB: `jdbc:postgresql://localhost:5433/song-db`
- Server port: `8081`

> Both services currently use `spring.jpa.hibernate.ddl-auto=update` for convenience. For production usage, a migration tool (e.g., Flyway/Liquibase) is recommended.

---

## Example usage

### Upload an MP3
```shell script
curl -X POST "http://localhost:8080/resources" \
  -H "Content-Type: audio/mpeg" \
  --data-binary "@./path/to/file.mp3"
```


Response contains the generated resource ID.

### Download an MP3
```shell script
curl -L "http://localhost:8080/resources/<id>" --output downloaded.mp3
```


### Delete resources (CSV)
```shell script
curl -X DELETE "http://localhost:8080/resources?id=1,2,3"
```


### Fetch song metadata
```shell script
curl "http://localhost:8081/songs/<id>"
```


---

## Notes on the Outbox pattern (implementation detail)

The outbox approach used here aims to provide:
- **atomicity**: write resource row + outbox event in the same transaction
- **retries**: failed deliveries increment attempts and store last error
- **batching**: scheduled jobs process events in chunks
- **idempotency**: song-service bulk create ignores conflicts for already-existing IDs

This is a practical compromise that works well for small systems and is a stepping stone toward more advanced setups (Kafka, Debezium, etc.) if needed later.

---

## Project structure

```
audio-library-system/
  resource-service/
  song-service/
  compose.yaml
  pom.xml
```


Each service is a separate Maven module under the root aggregator POM.