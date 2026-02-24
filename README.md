# Audio Library System

A simple, practical microservices playground using **Spring Boot**, **Spring Data JPA**, and **PostgreSQL**.
---

## The Services

- **resource-service**  
  Stores MP3 audio and extracts metadata (via Apache Tika).  
  Handles all uploads/downloads.

- **song-service**  
  Keeps track of song metadata.

- **eureka-server**  
  Registers and helps services find each other. Check the dashboard at [http://localhost:8761](http://localhost:8761).

- **api-gateway**  
  (Docker only!) Lets you call all backend APIs through one URL.

---

## How Everything Talks

All the services register with Eureka.  
When *in Docker Compose*, you call APIs via the gateway.  
When *developing locally*, call each service directly!

---

## How to Run

### Pre-requirements

- Java 21
- Maven (or just use `./mvnw`)
- Docker + Docker Compose

---

### Option 1: Run Locally for Development

1. **Start just the databases:**
   ```sh
   docker compose up -d resource-db song-db
   ```

2. **Start Eureka server:**
   ```sh
   ./mvnw -pl eureka-server spring-boot:run
   ```

3. **Start the services:**  
   *(In separate terminals or in your IDE)*
   ```sh
   ./mvnw -pl resource-service spring-boot:run
   ./mvnw -pl song-service spring-boot:run
   ./mvnw -pl api-gateway spring-boot:run
   ```

**Access URLs while running locally:**
- resource-service: [http://localhost:8080](http://localhost:8080)
- song-service: [http://localhost:8081](http://localhost:8081)
- api-gateway: http://localhost:8778(http://localhost:8778)
- eureka dashboard: [http://localhost:8761](http://localhost:8761)

---
#### **API Gateway Logging**

The `LoggingFilter` provides routing logs locally and in Docker. Example log output:

```
Incoming request: POST http://localhost:8778/song-service/songs | Route: song-service -> lb://song-service
Completed: POST http://localhost:8778/song-service/songs | Status: 201 CREATED | Time: 42 ms
```

### Option 2: Run Everything in Docker (Recommended!)

Just want it to work, with no manual steps?

```sh
docker compose up -d --build
```

Now, you call APIs ONLY through the **API Gateway**:

- resource-service: [http://localhost:8778/resource-service](http://localhost:8778/resource-service)
- song-service: [http://localhost:8778/song-service](http://localhost:8778/song-service)
- eureka dashboard: [http://localhost:8761](http://localhost:8761)

Stop everything with:

```sh
docker compose down
```

*(Pro tip: Your DBs won't persist data unless you add volumes.)*

---

## How to Call the APIs

### resource-service

| Method | Path                            | Local URL                                | Docker/Gateway URL                          | Description                                 |
|--------|---------------------------------|------------------------------------------|---------------------------------------------|---------------------------------------------|
| POST   | `/resources`                    | http://localhost:8080/resources          | http://localhost:8778/resource-service/resources | Upload MP3 audio (Content-Type: audio/mpeg) |
| GET    | `/resources/{id}`               | http://localhost:8080/resources/{id}     | http://localhost:8778/resource-service/resources/{id} | Download MP3 bytes by resource ID           |
| DELETE | `/resources?id=1,2,3` (CSV ids) | http://localhost:8080/resources?id=1,2,3 | http://localhost:8778/resource-service/resources?id=1,2,3 | Delete resource(s) by CSV of IDs            |

**Examples:**

_Local:_
```sh
curl -X POST "http://localhost:8080/resources" \
  -H "Content-Type: audio/mpeg" \
  --data-binary "@./your-file.mp3"

curl -L "http://localhost:8080/resources/123" --output file.mp3

curl -X DELETE "http://localhost:8080/resources?id=1,2,3"
```
_Docker Compose (through API Gateway):_
```sh
curl -X POST "http://localhost:8778/resource-service/resources" \
  -H "Content-Type: audio/mpeg" \
  --data-binary "@./your-file.mp3"

curl -L "http://localhost:8778/resource-service/resources/123" --output file.mp3

curl -X DELETE "http://localhost:8778/resource-service/resources?id=1,2,3"
```

---

### song-service

| Method | Path                         | Local URL                                 | Docker/Gateway URL                         | Description                               |
|--------|------------------------------|-------------------------------------------|--------------------------------------------|-------------------------------------------|
| POST   | `/songs`                     | http://localhost:8081/songs               | http://localhost:8778/song-service/songs   | Create a song metadata record             |
| GET    | `/songs/{id}`                | http://localhost:8081/songs/{id}          | http://localhost:8778/song-service/songs/{id} | Fetch song metadata by ID                 |
| DELETE | `/songs?id=1,2,3` (CSV ids)  | http://localhost:8081/songs?id=1,2,3      | http://localhost:8778/song-service/songs?id=1,2,3 | Delete one or more songs by IDs (CSV)     |

---

### Sample Request Body (for Creating a Song)

```json
{
  "id": 1,
  "name": "Sample Song",
  "artist": "Artist Name",
  "album": "Album Title",
  "duration": "01:40",
  "year": "2024"
}
```

---

### Validation Rules

- **All fields are required.**
- **id**: Numeric, must match an existing Resource ID.
- **name**: Text, 1-100 characters.
- **artist**: Text, 1-100 characters.
- **album**: Text, 1-100 characters.
- **duration**: String in `mm:ss` format, with leading zeros (e.g., `05:30`).
- **year**: 4-digit year in `YYYY` format, between 1900–2099.

---

**Examples:**

_Local:_
```sh
curl -X POST "http://localhost:8081/songs" \
  -H "Content-Type: application/json" \
  -d '{"name":"Sample Song","artist":"Artist","album":"Album","year":2024,"duration":01:23,"id":123}'

curl "http://localhost:8081/songs/123"

curl -X DELETE "http://localhost:8081/songs?id=1,2,3"
```
_Docker Compose (through API Gateway):_
```sh
curl -X POST "http://localhost:8778/song-service/songs" \
  -H "Content-Type: application/json" \
  -d '{"name":"Sample Song","artist":"Artist","album":"Album","year":2024,"duration":01:23,"id":123}'

curl "http://localhost:8778/song-service/songs/123"

curl -X DELETE "http://localhost:8778/song-service/songs?id=1,2,3"
```

## Project Structure

```
audio-library-system/
  api-gateway/
  eureka-server/
  init-scripts/
  resource-service/
  song-service/
  compose.yaml
  .env
```