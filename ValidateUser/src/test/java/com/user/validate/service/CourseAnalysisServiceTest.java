package com.user.validate.service;

import com.user.validate.controller.CourseProgressController;
import com.user.validate.model.CourseProgressEvent;
import com.user.validate.repository.CourseProgressEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CourseAnalysisServiceTest {

    @Mock
    private CourseProgressEventRepository repository;

    @InjectMocks
    private CourseProgressController service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAnalyzeCourseWithValidData() {
        // Arrange
        String courseId = "course123";
        List<CourseProgressEvent> mockEvents = List.of(
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_PASSED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_FAILED)
        );
        when(repository.findByCourseId(courseId)).thenReturn(mockEvents);

        // Act
        ResponseEntity<Map<String, Object>> res = service.analyzeCourse(courseId);

        var result = Objects.requireNonNull(res.getBody());

        System.out.println("result::::" + result.entrySet());

        // Assert
        assertEquals(2, result.get("participantsStarted"));
        assertEquals(1, result.get("participantsPassed"));
        assertEquals(1, result.get("participantsFailed"));
        assertEquals(50.0, result.get("passRate"));
    }

    @Test
    void testAnalyzeCourseWithNoPassOrFail() {
        // Arrange
        String courseId = "course123";
        List<CourseProgressEvent> mockEvents = List.of(
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_STARTED)
        );
        when(repository.findByCourseId(courseId)).thenReturn(mockEvents);

        // Act
        ResponseEntity<Map<String, Object>> res = service.analyzeCourse(courseId);

        var result = Objects.requireNonNull(res.getBody());


        System.out.println("result::::" + result.entrySet());

        // Assert
        assertEquals(2, result.get("participantsStarted"));
        assertEquals(0, result.get("participantsPassed"));
        assertEquals(0, result.get("participantsFailed"));
        assertEquals(0.0, result.get("passRate"));
    }

    @Test
    void testAnalyzeCourseWithNoEvents() {
        // Arrange
        String courseId = "course123";
        when(repository.findByCourseId(courseId)).thenReturn(List.of());

        // Act
        ResponseEntity<Map<String, Object>> res = service.analyzeCourse(courseId);

        var result = Objects.requireNonNull(res.getBody());
        System.out.println("result::::" + result.entrySet());

        // Assert
        assertEquals(0, result.get("participantsStarted"));
        assertEquals(0, result.get("participantsPassed"));
        assertEquals(0, result.get("participantsFailed"));
        assertEquals(0.0, result.get("passRate"));
    }

    @Test
    void testCreateEventSuccess() {
        // Arrange
        CourseProgressEvent event = createEvent("user1", "course1", CourseProgressEvent.EventType.COURSE_STARTED);

        // Act
        ResponseEntity<String> response = service.createEvent(event);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Event created", response.getBody());
        verify(repository, times(1)).save(event);
    }

    @Test
    void testCreateEventValidationFailure() {
        // Arrange
        CourseProgressEvent event = new CourseProgressEvent(); // Missing required fields

        // Act
        ResponseEntity<String> response = service.createEvent(event);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid event data", response.getBody());
        verify(repository, never()).save(any());
    }

    @Test
    void testGetEventsByUserSuccess() {
        // Arrange
        String userId = "user1";
        List<CourseProgressEvent> mockEvents = List.of(
                createEvent(userId, "course1", CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent(userId, "course2", CourseProgressEvent.EventType.COURSE_PASSED)
        );
        when(repository.findByUserIdOrderByTimestamp(userId)).thenReturn(mockEvents);

        // Act
        ResponseEntity<List<CourseProgressEvent>> response = service.getEventsByUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(repository, times(1)).findByUserIdOrderByTimestamp(userId);
    }

    @Test
    void testGetEventsByUserNoEvents() {
        // Arrange
        String userId = "user1";
        when(repository.findByUserIdOrderByTimestamp(userId)).thenReturn(List.of());

        // Act
        ResponseEntity<List<CourseProgressEvent>> response = service.getEventsByUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(repository, times(1)).findByUserIdOrderByTimestamp(userId);
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