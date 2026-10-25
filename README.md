# RunTracker
RunTracker is a RESTful backend application for managing and analyzing running activities. 
It provides CRUD operations, filtering, statistics, pagination, validation and automated tests 
using Spring Boot and PostgreSQL.

![CI](https://github.com/Daniel-Trot/RunTracker/actions/workflows/ci.yaml/badge.svg)

## Features
- Create, read, update and delete training activities
- Filter trainings by minimum distance
- Filter trainings by date range
- Calculate training statistics
- Pagination and sorting
- Request validation
- Global error handling
- DTO-based request and response handling

## Tech Stack
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Bean Validation
- JUnit 5
- Mockito
- MockMvc
- Maven
- Docker
- Docker Compose
- GitHub Actions
- OpenAPI / Swagger
- Testcontainers

## Architecture
```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Additional components:
- Request and response DTOs
- Global exception handler
- Bean Validation

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/trainings` | Create a new training activity |
| GET | `/api/trainings/{id}` | Get a training by ID |
| GET | `/api/trainings?page=0&size=10&sort=date,asc` | Get trainings with pagination and sorting |
| PUT | `/api/trainings/{id}` | Update an existing training |
| DELETE | `/api/trainings/{id}` | Delete a training |
| GET | `/api/trainings/filter?minDistance=10` | Filter trainings by minimum distance |
| GET | `/api/trainings/by-date?from=2026-10-01&to=2026-10-31` | Filter trainings by date range |
| GET | `/api/trainings/stats` | Get statistics for all trainings |
| GET | `/api/trainings/stats?from=2026-10-01&to=2026-10-31` | Get statistics for a specific date range |

### Example Request

Create a new training:

```http
POST /api/trainings
Content-Type: application/json
```

```json
{
  "date": "2026-10-25",
  "distanceKm": 10.0,
  "durationMinutes": 50,
  "averageHeartRate": 145
}
```

### Example Response

A successful request returns HTTP `201 Created`:

```json
{
  "id": 1,
  "date": "2026-10-25",
  "distanceKm": 10.0,
  "durationMinutes": 50,
  "averageHeartRate": 145,
  "pace": 5.0
}
```

The `pace` value is calculated automatically from the training duration and distance.

Interactive API documentation is available through Swagger UI when the application is running:

```text
http://localhost:8080/swagger-ui.html
```

## Validation & Error Handling
Incoming training requests are validated using Jakarta Bean Validation.

Examples:
- Date must not be null
- Distance must be greater than 0
- Duration must be greater than 0
- Average heart rate must be between 30 and 250

Validation errors are handled centrally using a global exception handler 
and returned as structured HTTP 400 responses.

Missing trainings are returned as HTTP 404 responses.

## Testing

The project includes unit, controller and integration tests.

- `TrainingServiceTest` uses JUnit 5 and Mockito to test business logic independently of the database.
- `TrainingControllerTest` uses MockMvc with a mocked service layer to test REST endpoints, validation and HTTP status codes.
- `TrainingIntegrationTest` uses Testcontainers with PostgreSQL to test the complete flow from controller to service to repository.
- GitHub Actions automatically runs the test suite on every push and pull request.

## Setup

### Prerequisites

Make sure the following software is installed:

- Java 21 or newer
- PostgreSQL (for local setup without Docker)
- Git
- IntelliJ IDEA or another Java IDE (optional)
- Docker (optional, for containerized setup)

The project includes the Maven Wrapper, so a separate Maven installation is not required.

### 1. Clone the repository

```bash
git clone https://github.com/Daniel-Trot/RunTracker
cd RunTracker
```

### 2. Create the PostgreSQL database

Create a local PostgreSQL database named:

```text
runtracker
```

For example, using pgAdmin:

1. Connect to your local PostgreSQL server
2. Right-click **Databases**
3. Select **Create → Database**
4. Enter `runtracker` as the database name

### 3. Configure the database connection
The PostgreSQL connection is configured in:

```text
src/main/resources/application.properties
```

The application expects the database password through the `DB_PASSWORD` environment variable:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/runtracker
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Set the `DB_PASSWORD` environment variable to your local PostgreSQL password before starting the application.

In IntelliJ IDEA, this can be configured under:

```text
Run → Edit Configurations → Environment variables
```

Example:

```text
DB_PASSWORD=your_postgresql_password
```

### 4. Start the application

On Windows:

```bash
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

Alternatively, start the Spring Boot application directly from your IDE.

The application will run on:

```text
http://localhost:8080
```

### 5. Test the API

The API can be tested using Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Swagger provides interactive documentation for all available endpoints and allows requests to be executed directly from the browser.

Alternatively, the API can be tested with tools such as Postman, IntelliJ HTTP Client or curl.

### 6. Run with Docker

Docker can be used to start both the RunTracker backend and PostgreSQL without requiring a locally configured PostgreSQL database.

Create a `.env` file in the project root:

```env
POSTGRES_PASSWORD=your_postgresql_password
```

The `.env` file is excluded from Git and should not be committed.

Start the application and PostgreSQL with:

```bash
docker compose up --build
```

Docker Compose will start:

- the RunTracker Spring Boot backend
- a PostgreSQL 17 database
- a persistent Docker volume for the database data

The application will be available at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

To stop the containers:

```bash
docker compose down
```

Database data is stored in a Docker volume and remains available after the containers are stopped or restarted.

## Future Improvements

The project is actively being developed. Planned improvements include:

- Frontend integration for managing and analyzing training activities
- More advanced analytics
- Additional integration test coverage