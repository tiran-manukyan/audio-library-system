# Audio Library System

A small, pragmatic microservices project built with **Spring Boot + Spring Data JPA + PostgreSQL**, split into two services:

- **resource-service**: stores MP3 bytes and extracts metadata (via Apache Tika).
- **song-service**: stores and serves song metadata.

The services communicate over HTTP.

---

## Architecture (high level)

### Services

#### 1) `resource-service`
Responsibilities:
- Accept an MP3 upload and store the raw bytes in its DB.
- Extract metadata from the audio (name/artist/album/year/duration).
- Send metadata to `song-service` via HTTP.

API (core):
- `POST /resources` (`Content-Type: audio/mpeg`) → stores file, returns generated resource ID
- `GET /resources/{id}` → returns MP3 bytes (`audio/mpeg`)
- `DELETE /resources?id=<csv>` → deletes resources by CSV list of IDs

#### 2) `song-service`
Responsibilities:
- Persist metadata records keyed by `id` (same ID as resource ID).
- Support single create and delete operations.

API (core):
- `POST /songs` → create one song metadata record
- `GET /songs/{id}` → fetch metadata
- `DELETE /songs?id=<csv>` → delete by CSV list of IDs

---

## Data & communication flow

### Upload flow
1. Client uploads an MP3 to `resource-service`.
2. `resource-service` extracts metadata and stores MP3 bytes.
3. `resource-service` sends metadata to `song-service` via HTTP POST.

### Delete flow
1. Client requests delete on `resource-service` using a CSV list of resource IDs.
2. `resource-service` deletes rows from its DB.
3. `resource-service` calls `song-service` to delete metadata for those IDs.

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

```shell
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
```shell
./mvnw -pl song-service spring-boot:run
```

**resource-service**
```shell
./mvnw -pl resource-service spring-boot:run
```

#### Option B: build jars then run
```shell
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
- HTTP timeouts for song-service communication

### song-service
`song-service/src/main/resources/application.properties` includes:
- DB: `jdbc:postgresql://localhost:5433/song-db`
- Server port: `8081`

> Both services currently use `spring.jpa.hibernate.ddl-auto=update` for convenience. For production usage, a migration tool (e.g., Flyway/Liquibase) is recommended.

---

## Example usage

### Upload an MP3
```shell
curl -X POST "http://localhost:8080/resources" \
  -H "Content-Type: audio/mpeg" \
  --data-binary "@./path/to/file.mp3"
```

Response contains the generated resource ID.

### Download an MP3
```shell
curl -L "http://localhost:8080/resources/<id>" --output downloaded.mp3
```

### Delete resources (CSV)
```shell
curl -X DELETE "http://localhost:8080/resources?id=1,2,3"
```

### Fetch song metadata
```shell
curl "http://localhost:8081/songs/<id>"
```

---

## Project structure

```
audio-library-system/
  resource-service/
  song-service/
  compose.yaml
```