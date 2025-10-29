package com.user.validate.controller;

import com.user.validate.ValidateUserApplication;
import com.user.validate.model.CourseProgressEvent;
import com.user.validate.repository.CourseProgressEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Integration tests for the CourseProgressController.
 * This class tests the RESTful endpoints of the controller in a fully integrated
 * Spring Boot environment with an in-memory database.
 *
 * Key Test Scenarios:
 * - Verifies the creation of course progress events via the POST endpoint.
 * - Validates the retrieval of events for a specific user via the GET endpoint.
 * - Tests the analysis of course metrics, including participant counts and pass rates.
 *
 * The tests ensure proper HTTP status codes, response structures, and data processing logic.
 */
@SpringBootTest(classes = ValidateUserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourseProgressControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CourseProgressEventRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    /**
     * Tests the creation of a course progress event via the POST endpoint - /v1/events.
     * Verifies that the event is successfully created, the HTTP status is 201 (CREATED),
     * and the event is persisted in the database.
     */
    @Test
    void testCreateEvent() {
        CourseProgressEvent event = new CourseProgressEvent();
        event.setUserId("user1");
        event.setCourseId("course1");
        event.setEventType(CourseProgressEvent.EventType.COURSE_STARTED);
        event.setTimestamp(Instant.now());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/events",
                event,
                String.class
        );

        // Asserttionstions
        System.out.println("Response Body: " + response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Event created", response.getBody());
        assertEquals(1, repository.count());
    }


    /**
     * Tests the analysis of course metrics via the GET endpoint /analysis/course/$courseId.
     * Verifies that the correct participant counts and pass rate are returned
     * for a specific course. Ensures the HTTP status is 200 (OK) and the response
     * contains the expected analysis data.
     */
    @Test
    void testAnalyzeCourse() {
        String courseId = "course123";
        repository.saveAll(List.of(
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_PASSED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_FAILED)
        ));

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/v1/events/analysis/course/" + courseId,
                Map.class
        );

        // Asserttions
        System.out.println("Response Body: " + response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> result = response.getBody();
        assertEquals(2, result.get("participantsStarted"));
        assertEquals(1, result.get("participantsPassed"));
        assertEquals(1, result.get("participantsFailed"));
        assertEquals(50.0, result.get("passRate"));
    }

    /**
     * Tests the retrieval of all events for a specific user via the GET endpoint /v1/events/user/$userId.
     * Verifies that the events are returned in chronological order, the HTTP status
     * is 200 (OK), and the response contains the correct number of events.
     */
    @Test
    void testGetEventsByUser() {
        String userId = "user1";
        repository.saveAll(List.of(
                createEvent(userId, "course1", CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent(userId, "course2", CourseProgressEvent.EventType.COURSE_PASSED)
        ));

        ResponseEntity<CourseProgressEvent[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/v1/events/user/" + userId,
                CourseProgressEvent[].class
        );

        // Asserttions
        System.out.println("Response Body: " + response.getBody().length);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
    }

    // Helper method to create a CourseProgressEvent
    private CourseProgressEvent createEvent(String userId, String courseId, CourseProgressEvent.EventType eventType) {
        CourseProgressEvent event = new CourseProgressEvent();
        event.setUserId(userId);
        event.setCourseId(courseId);
        event.setEventType(eventType);
        event.setTimestamp(Instant.now());
        return event;
    }
}