# ValidateUser

## Overview
The `ValidateUser` project is a microservice built using Java and Spring Boot. It is designed to handle and analyze course progress events for users. The service provides RESTful endpoints for event management and course analysis.

---

## Features
- **Event Management**: Accepts and stores course progress events.
- **User Event Retrieval**: Fetches all events for a specific user in chronological order.
- **Course Analysis**: Provides metrics for a specific course, including participant counts and pass rates.

---

## Data Model
The `CourseProgressEvent` data model includes the following fields:
- `eventId` (UUID): Unique identifier generated on the server side.
- `userId` (String): Unique identifier for the user.
- `courseId` (String): Unique identifier for the course.
- `timestamp` (ISO 8601 String): Time when the event occurred.
- `eventType` (Enum): Type of progress event. Supported values:
    - `COURSE_STARTED`
    - `COURSE_PASSED`
    - `COURSE_FAILED`

---

## REST Endpoints

### 1. **POST /v1/events**
- **Description**: Accepts a single `CourseProgressEvent`, validates for all the fields, and stores it.

### 2. **GET /v1/events/user/{userId}**
- **Description**: Returns a chronologically sorted list of all events for a specific user.

### 3. **GET /v1/analysis/course/{courseId}**
- **Description**: Performs an analysis for a specific course and returns the following metrics:
    - `participantsStarted`: Total number of unique users who started the course.
    - `participantsPassed`: Total number of unique users who passed the course.
    - `participantsFailed`: Total number of unique users who failed the course.
    - `passRate`: Success rate in percent (`passed / (passed + failed)`).
---

## Test Strategy is followed as below for this project

### 1. **Unit Tests**
- **Goal**: Test core business logic independently.
- **Approach**: Used Mockito to mock the repository layer.
- **Checks**: Participant count and pass rate calculations.


### 2. **Integration Tests**
- **Goal**: Validate API endpoints and controller flow.
- **Approach**: 
    - Used @SpringBootTest with an in-memory DB.
    - Verified status codes, JSON responses, and data handling.

### 3. **Code Coverage**
- **Tool**: JaCoCo Maven Plugin.
- **Purpose**: Generate coverage reports and ensure meaningful test coverage for core logic.

### 4. **Future Enhancements**
- This setup can be extended with:
    - **End-to-End Tests**: To verify full workflows and external integrations. 
    - Performance Tests: To measure stability and response times under load.

---

## Build and Run

### Prerequisites
- Java 17
- Maven

### Build
```bash
mvn clean install
```
### Run
```bash
mvn spring-boot:run
```
## Code Coverage
The project uses JaCoCo for code coverage. To generate a coverage report
after running tests, use:
```bash
mvn test
mvn jacoco:report
```
The coverage report will look similar to the example below:
![img.png](../covergaeReport.png)

### Use of AI Tools

AI-assisted tools such as ChatGPT and GitHub Copilot were used during the development of this project to improve efficiency and streamline repetitive coding tasks. All AI-generated content was manually reviewed, verified, and adapted to ensure correctness and maintainability.

## AI Was Used to Generate

* **Boilerplate Code**:
Created initial scaffolding for controller, service, and repository classes to accelerate project setup.

* **Testing**:
Drafted unit and integration test templates, identified potential edge cases (e.g., division-by-zero), and generated sample test data.

* **CI/CD Pipeline**:
Helped outline the initial structure for the GitHub Actions workflow.

## Documentation & Readability:
Assisted in phrasing sections of the README (e.g., Test Strategy) and refining code comments for better clarity.

## Example Prompt Used
```
“Generate a Spring Boot controller and service for handling CourseProgressEvent objects (fields: eventId, userId, courseId, timestamp, eventType).
Include validation for all fields, a POST endpoint to store events, and an analysis endpoint to calculate passRate.
Use H2 for persistence and handle error scenarios gracefully.”
```

## Manual Intervention & Review
```
All AI-assisted suggestions were manually inspected, rewritten, and validated before inclusion.

Key areas requiring manual effort:
* Finalizing business logic and validation.
* Writing precise test assertions and ensuring edge-case coverage.
* Optimized AI-generated code for readability, structure, and maintainability.
```
## Reflection
AI tools improved development speed and consistency, especially in repetitive or boilerplate-heavy areas.
However, core logic, architectural decisions, debugging, and optimization were driven by human expertise to ensure correctness, scalability, and production readiness.