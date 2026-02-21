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
- Docker + Docker Compose

---

## Running the project

### Prerequisites
- Java 21
- Maven (or use the included Maven Wrapper)
- Docker + Docker Compose

### Option A: Local development (services in IntelliJ / on host, DBs in Docker)
This is the most convenient dev loop.

#### 1) Start only the databases
From the repository root:
```shell
docker compose up -d resource-db song-db
```
This starts:
- `resource-db` (database `resource-db`)
- `song-db` (database `song-db`)

> Credentials and URLs are configured to work both locally and in Docker (see `compose.yaml`, `.env`, and service `application.properties`).

#### 2) Run services
In two terminals:

**song-service**
```shell
./mvnw -pl song-service spring-boot:run
```
**resource-service**
```shell
./mvnw -pl resource-service spring-boot:run
```
#### 3) Verify
- resource-service: http://localhost:8080
- song-service: http://localhost:8081

---

### Option B: Docker Compose (everything in containers)
Build and start all services and databases:
```shell
docker compose up -d --build
```
Verify:
- resource-service: http://localhost:8080
- song-service: http://localhost:8081

To stop everything:
```shell
docker compose down
```
> Note: database data is not persisted between restarts (no mounted data volume).

---

## Configuration notes

- Database schema is initialized by SQL scripts mounted into the PostgreSQL containers (see `init-scripts/`).
- Hibernate DDL auto-generation is disabled (`spring.jpa.hibernate.ddl-auto=none`).

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
init-scripts/
resource-service/
song-service/
compose.yaml
.env
```