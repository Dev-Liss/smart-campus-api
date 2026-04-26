# Smart Campus API

## Overview

The Smart Campus API is a robust, resource-oriented RESTful web service for managing rooms and IoT sensors across a university campus. Built with **Java 8**, **Jersey 2.32** (JAX-RS), and **Apache Tomcat 9**, it exposes full Create, Read, Update, and Delete operations across two core resources — Rooms and Sensors — plus a nested SensorReadings sub-resource. All state is held in thread-safe, in-memory `ConcurrentHashMap`s via a singleton `DataStore`, with no external database or Spring Boot dependency. The API follows REST conventions throughout: JSON for all payloads, standard HTTP verbs and status codes, a HATEOAS discovery endpoint, comprehensive `ExceptionMapper`s, and a cross-cutting request/response logging filter.

---

## Tech Stack

| Component            | Technology                                                                          |
| -------------------- | ----------------------------------------------------------------------------------- |
| Language             | Java 8                                                                              |
| JAX-RS Runtime       | Jersey 2.32 (`jersey-container-servlet`, `jersey-hk2`, `jersey-media-json-jackson`) |
| JSON Provider        | Jackson (via `jersey-media-json-jackson`)                                           |
| Dependency Injection | HK2 (Jersey built-in)                                                               |
| Application Server   | Apache Tomcat 9.x                                                                   |
| Build Tool           | Maven 3.x (`war` packaging)                                                         |
| In-Memory Storage    | `ConcurrentHashMap` singleton — no database                                         |

---

## Package Structure

| Package                     | Purpose                                                      |
| --------------------------- | ------------------------------------------------------------ |
| `com.smartcampus`           | JAX-RS bootstrap (`SmartCampusApplication`)                  |
| `com.smartcampus.model`     | POJOs: `Room`, `Sensor`, `SensorReading`, `ApiErrorResponse` |
| `com.smartcampus.store`     | Singleton in-memory `DataStore` (`ConcurrentHashMap`)        |
| `com.smartcampus.resource`  | JAX-RS resource and sub-resource classes                     |
| `com.smartcampus.exception` | Custom exceptions and `ExceptionMapper` providers            |
| `com.smartcampus.filter`    | `ApiLoggingFilter` — request/response logging                |

---

## API Endpoint Reference

| Method   | Endpoint                              | Description                                         | Success Code |
| -------- | ------------------------------------- | --------------------------------------------------- | ------------ |
| `GET`    | `/api/v1/`                            | Discovery — returns API metadata and resource links | 200          |
| `GET`    | `/api/v1/rooms`                       | Retrieve all rooms                                  | 200          |
| `POST`   | `/api/v1/rooms`                       | Create a new room                                   | 201          |
| `GET`    | `/api/v1/rooms/{roomId}`              | Retrieve a specific room by ID                      | 200          |
| `PUT`    | `/api/v1/rooms/{roomId}`              | Update an existing room's name and capacity         | 200          |
| `DELETE` | `/api/v1/rooms/{roomId}`              | Delete a room (blocked if sensors are assigned)     | 204          |
| `GET`    | `/api/v1/sensors`                     | Retrieve all sensors (supports `?type=` filter)     | 200          |
| `POST`   | `/api/v1/sensors`                     | Register a new sensor (validates `roomId` exists)   | 201          |
| `GET`    | `/api/v1/sensors/{sensorId}`          | Retrieve a specific sensor by ID                    | 200          |
| `PUT`    | `/api/v1/sensors/{sensorId}`          | Update sensor type, status, or current value        | 200          |
| `DELETE` | `/api/v1/sensors/{sensorId}`          | Delete a sensor and remove it from its parent room  | 204          |
| `GET`    | `/api/v1/sensors/{sensorId}/readings` | Retrieve full reading history for a sensor          | 200          |
| `POST`   | `/api/v1/sensors/{sensorId}/readings` | Append a new reading (updates parent sensor value)  | 201          |

### Error Response Map

| Scenario                                         | Status Code                 | Exception                         |
| ------------------------------------------------ | --------------------------- | --------------------------------- |
| Room still has sensors assigned (DELETE blocked) | `409 Conflict`              | `RoomNotEmptyException`           |
| Sensor references a non-existent `roomId`        | `422 Unprocessable Entity`  | `LinkedResourceNotFoundException` |
| Posting a reading to a `MAINTENANCE` sensor      | `403 Forbidden`             | `SensorUnavailableException`      |
| Any unexpected runtime error                     | `500 Internal Server Error` | `GenericExceptionMapper`          |

All error bodies share the same `ApiErrorResponse` JSON structure:

```json
{
  "error": "Conflict",
  "message": "Room has active sensors",
  "timestamp": 1714109200000
}
```

---

## Build & Deploy

### Prerequisites

- JDK 8 or later installed with `JAVA_HOME` set
- Apache Maven 3.6+ on the `PATH`
- Apache Tomcat 9.x downloaded and extracted

### Step 1 — Build the WAR

```bash
mvn clean package
```

This produces `target/smart-campus-api-1.0-SNAPSHOT.war`.

### Step 2 — Deploy to Tomcat

```bash
# Linux / macOS
cp target/smart-campus-api-1.0-SNAPSHOT.war /opt/tomcat/webapps/smart-campus-api.war

# Windows (PowerShell)
Copy-Item target\smart-campus-api-1.0-SNAPSHOT.war C:\tomcat9\webapps\smart-campus-api.war
```

### Step 3 — Start Tomcat

```bash
# Linux / macOS
/opt/tomcat/bin/startup.sh

# Windows
C:\tomcat9\bin\startup.bat
```

The API is now available at `http://localhost:8080/smart-campus-api/api/v1/`.

### Step 4 — Stop Tomcat

```bash
# Linux / macOS
/opt/tomcat/bin/shutdown.sh

# Windows
C:\tomcat9\bin\shutdown.bat
```

> **Tip:** Dropping a new WAR into `webapps/` while Tomcat is running triggers an automatic hot-redeploy.

---

## Sample curl Commands

> All examples target `http://localhost:8080/smart-campus-api`. Adjust host/port as needed.

### 1. Discovery Endpoint (HATEOAS)

```bash
curl -s http://localhost:8080/smart-campus-api/api/v1/
```

**Expected 200 response:**

```json
{
  "apiVersion": "1.0",
  "description": "Smart Campus Sensor & Room Management API",
  "contact": "admin@smartcampus.com",
  "endpoints": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

### 2. Create a Room (201 Created)

```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Library Study Zone 1", "capacity": 25}'
```

### 3. Register a Sensor to a Room (201 Created)

```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "status": "ACTIVE", "currentValue": 22.5, "roomId": "room-1"}'
```

### 4. Attempt to Delete an Occupied Room (409 Conflict)

```bash
curl -i -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/room-1
```

**Expected 409 response:**

```json
{
  "error": "Conflict",
  "message": "Room has active sensors",
  "timestamp": 1714109200000
}
```

### 5. Post a New Sensor Reading — Sub-Resource (201 Created)

```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/sensor-1/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.1}'
```

### 6. Create a Sensor with a Non-Existent Room (422 Unprocessable Entity)

```bash
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "CO2", "status": "ACTIVE", "currentValue": 0.0, "roomId": "FAKE-ROOM"}'
```

### 7. Filter Sensors by Type (Query Parameter)

```bash
curl -s "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"
```

### 8. Post a Reading to a MAINTENANCE Sensor (403 Forbidden)

```bash
# Step 1 — set a sensor to MAINTENANCE
curl -i -X PUT http://localhost:8080/smart-campus-api/api/v1/sensors/sensor-1 \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "status": "MAINTENANCE", "currentValue": 22.5}'

# Step 2 — attempt to post a reading (blocked)
curl -i -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/sensor-1/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 10.5}'
```

---

## Conceptual Report (Theory Questions)

### Part 1: Service Architecture & Setup (10 Marks)

#### 1. In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions

By default, JAX-RS resource classes are request-scoped, meaning a new instance is created for every incoming HTTP request and destroyed once the response is sent. This ensures clean isolation between concurrent requests but causes any instance-level state to be lost immediately. To persist data across requests, our implementation uses a singleton `DataStore` that holds a single eagerly-initialized static instance accessed via `DataStore.getInstance()`. All shared data resides in `ConcurrentHashMap` collections, which are inherently thread-safe for individual operations like put, get, and remove, thereby preventing race conditions. The readings list uses `computeIfAbsent` for atomic initialization. Since the `DataStore` is created once at JVM startup, every request-scoped resource instance shares the same persistent in-memory state throughout the Tomcat process lifetime.

---

#### 2. Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS (Hypermedia As The Engine Of Application State) represents REST Maturity Level 3 in the Richardson Maturity Model. Rather than requiring clients to hard-code URIs, the API embeds navigable links directly in responses, making it self-describing at runtime. Our `DiscoveryResource` at `GET /api/v1/` returns a JSON map with live URIs for rooms and sensors endpoints. This provides three key benefits: resilience to change, as clients following embedded links adapt automatically when server paths are restructured; discoverability, allowing new clients to traverse the entire API surface from the root endpoint without external documentation; and reduced coupling, since the server owns navigation logic and clients simply consume it, eliminating version skew between documentation and the actual API.

---

### Part 2: Room Management (20 Marks)

#### 1. When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing

Returning only IDs reduces payload size and network bandwidth since the response is a compact string array, but it creates the N+1 problem where the client must issue a separate `GET /rooms/{id}` request for each ID, multiplying round-trips, increasing latency, and complicating client-side rendering logic. Returning full room objects, which is our chosen approach, produces a larger initial payload but gives the client everything it needs in a single request, enabling immediate rendering without additional calls. Since room metadata (name, capacity, sensor IDs) is small and bounded, the bandwidth cost is negligible compared to the dramatic reduction in client complexity and server load. For very large datasets, a middle ground of summary representations with HATEOAS links to full details offers the best of both approaches.

---

#### 2. Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Our `DELETE /api/v1/rooms/{roomId}` is not fully idempotent in the strict HTTP sense. The first successful call removes the room and returns `204 No Content`, but subsequent identical calls return `404 Not Found` because the resource no longer exists in the `DataStore`. While the server state after both calls is identical (the room is gone), satisfying idempotency at the resource-state level, the HTTP status code changes between calls. This is a deliberate design choice: returning `404` on repeated deletes distinguishes "successfully deleted just now" from "already gone," which is more informative than silently returning `204` regardless. Our implementation checks existence via `dataStore.getRooms().get(roomId)` and returns a structured `ApiErrorResponse` when the room is absent.

---

### Part 3: Sensor Operations & Linking (20 Marks)

#### 1. We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares a content-type contract at the method level. When a client sends a request with a mismatched `Content-Type` header such as `text/plain` or `application/xml`, JAX-RS performs content negotiation at the framework layer before any Java method body executes. Jersey inspects the incoming header, finds no matching `@Consumes` declaration among registered resource methods, and automatically returns HTTP `415 Unsupported Media Type` without ever invoking the resource method or touching the `DataStore`. This is handled entirely by the JAX-RS runtime, requiring zero defensive code in the resource class. The client receives a clear, specific signal that the problem is the format of their request body, which is far more informative than a generic `400 Bad Request`.

---

#### 2. You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

In REST, a URI identifies a resource — a distinct addressable entity. Using `/api/v1/sensors/type/CO2` misleadingly implies `type` and `CO2` are sub-resources, when in reality "all CO2 sensors" is just a filtered view of the `/sensors` collection. The `@QueryParam("type")` approach is superior for several reasons: semantic accuracy, as `GET /sensors?type=CO2` clearly requests a filtered view of the same collection; composability, since multiple filters stack naturally like `?type=CO2&status=ACTIVE` without path explosion; optional by nature, as omitting the parameter returns the full collection with no special routing needed (our `getAllSensors()` returns all values when type is null); and caching friendliness, since HTTP caches treat query parameters as variants of the same resource rather than separate resources.

---

### Part 4: Deep Nesting with Sub-Resources (20 Marks)

#### 1. Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

A sub-resource locator is a JAX-RS method with a `@Path` annotation but no HTTP verb annotation. In our `SensorResource`, the `getReadingsResource()` method is annotated with `@Path("/{sensorId}/readings")` and returns a `SensorReadingResource` instance, letting Jersey resolve HTTP verbs against the returned object. This provides single responsibility where `SensorResource` handles sensor CRUD and `SensorReadingResource` handles reading CRUD independently. It eliminates the "God Class" anti-pattern that arises from encoding every nested path in one class. The `sensorId` is captured once in the locator and passed via the constructor, so all methods operate in the correct context. Adding new operations like `DELETE /readings/{rid}` requires changes only in `SensorReadingResource`, with zero modifications to `SensorResource`.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging (30 Marks)

#### 1. Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

HTTP `404 Not Found` is semantically tied to the URI itself being unreachable — the endpoint `/api/v1/sensors` exists and is fully operational. When a client POSTs valid JSON like `{"type": "CO2", "roomId": "FAKE-ROOM"}` to a valid endpoint, the server successfully parses the JSON and matches the route. The problem lies in the payload content: `roomId` references a non-existent room. HTTP `422 Unprocessable Entity` precisely fits this scenario, meaning "the server understood the content type and parsed the entity, but cannot process it due to a semantic error." Our `LinkedResourceNotFoundExceptionMapper` catches this and returns 422 with a structured `ApiErrorResponse`, giving the client an accurate signal to fix the data in their request body rather than the URL.

---

#### 2. From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Exposing Java stack traces is a serious information disclosure vulnerability. An attacker can extract framework and version details like `org.glassfish.jersey.server-2.32` to cross-reference against CVE databases for known exploits. Internal package names such as `com.smartcampus.store.DataStore.getRooms(DataStore.java:48)` reveal the application architecture, aiding targeted attacks. File system paths expose deployment topology, while JDBC exception messages may leak connection strings or credentials. Line numbers and method names can reveal business logic and security decision points. Our `GenericExceptionMapper` implements `ExceptionMapper<Throwable>` as a catch-all, logging the full exception server-side via `java.util.logging.Logger` for engineer visibility while returning only a safe, opaque `500 Internal Server Error` with a generic `ApiErrorResponse` to the client.

---

#### 3. Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

JAX-RS filters apply the DRY and Separation of Concerns principles at the framework level. Our `ApiLoggingFilter` implements both `ContainerRequestFilter` and `ContainerResponseFilter` in a single class, automatically logging every request and response flowing through Jersey — including requests to paths without explicit resource methods. This eliminates code duplication and ensures guaranteed coverage regardless of whether developers remember to add logging to new classes. Maintainability improves since changing the logging format requires editing only one file instead of scattered statements. Resource methods remain pure business logic without interleaved logging boilerplate. The filter lifecycle also provides accurate timing data, firing before method execution and after response construction, which inline statements cannot reliably capture.

---

## Full Project Structure

```
src/main/java/com/smartcampus/
├── SmartCampusApplication.java              # JAX-RS bootstrap — registers all resources & providers
├── model/
│   ├── Room.java                            # Room entity (id, name, capacity, sensorIds)
│   ├── Sensor.java                          # Sensor entity (id, type, status, currentValue, roomId)
│   ├── SensorReading.java                   # Reading record (id, timestamp, value)
│   └── ApiErrorResponse.java                # Uniform JSON error body
├── store/
│   └── DataStore.java                       # Singleton ConcurrentHashMap store with seed data
├── resource/
│   ├── DiscoveryResource.java               # GET /api/v1/ — HATEOAS discovery
│   ├── RoomResource.java                    # Full CRUD — /api/v1/rooms
│   ├── SensorResource.java                  # Full CRUD + ?type= filter — /api/v1/sensors
│   └── SensorReadingResource.java           # Sub-resource — /api/v1/sensors/{id}/readings
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   ├── SensorUnavailableException.java
│   ├── RoomNotEmptyExceptionMapper.java           # → 409 Conflict
│   ├── LinkedResourceNotFoundExceptionMapper.java  # → 422 Unprocessable Entity
│   ├── SensorUnavailableExceptionMapper.java       # → 403 Forbidden
│   └── GenericExceptionMapper.java                 # → 500 Internal Server Error
└── filter/
    └── ApiLoggingFilter.java                # ContainerRequest + ContainerResponse logging

src/main/webapp/WEB-INF/
└── web.xml                                  # Jersey ServletContainer registration

pom.xml                                      # Maven WAR build — Java 8, Jersey 2.32
```
