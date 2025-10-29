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

    @Test
    void testCreateEvent() {
        // Arrange
        CourseProgressEvent event = new CourseProgressEvent();
        event.setUserId("user1");
        event.setCourseId("course1");
        event.setEventType(CourseProgressEvent.EventType.COURSE_STARTED);
        event.setTimestamp(Instant.now());

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/events",
                event,
                String.class
        );

        // Assert
        System.out.println("Response Body: " + response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Event created", response.getBody());
        assertEquals(1, repository.count());
    }

    @Test
    void testAnalyzeCourse() {
        // Arrange
        String courseId = "course123";
        repository.saveAll(List.of(
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_PASSED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_FAILED)
        ));

        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/v1/events/analysis/course/" + courseId,
                Map.class
        );

        // Assert
        System.out.println("Response Body: " + response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> result = response.getBody();
        assertEquals(2, result.get("participantsStarted"));
        assertEquals(1, result.get("participantsPassed"));
        assertEquals(1, result.get("participantsFailed"));
        assertEquals(50.0, result.get("passRate"));
    }

    @Test
    void testGetEventsByUser() {
        // Arrange
        String userId = "user1";
        repository.saveAll(List.of(
                createEvent(userId, "course1", CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent(userId, "course2", CourseProgressEvent.EventType.COURSE_PASSED)
        ));

        // Act
        ResponseEntity<CourseProgressEvent[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/v1/events/user/" + userId,
                CourseProgressEvent[].class
        );

        // Assert
        System.out.println("Response Body: " + response.getBody().length);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().length);
    }

    private CourseProgressEvent createEvent(String userId, String courseId, CourseProgressEvent.EventType eventType) {
        CourseProgressEvent event = new CourseProgressEvent();
        event.setUserId(userId);
        event.setCourseId(courseId);
        event.setEventType(eventType);
        event.setTimestamp(Instant.now());
        return event;
    }
}