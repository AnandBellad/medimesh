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
         
        String courseId = "course123";
        List<CourseProgressEvent> mockEvents = List.of(
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_PASSED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_FAILED)
        );
        when(repository.findByCourseId(courseId)).thenReturn(mockEvents);

        
        ResponseEntity<Map<String, Object>> res = service.analyzeCourse(courseId);

        var result = Objects.requireNonNull(res.getBody());

        System.out.println("result::::" + result.entrySet());

        // Asserttions
        assertEquals(2, result.get("participantsStarted"));
        assertEquals(1, result.get("participantsPassed"));
        assertEquals(1, result.get("participantsFailed"));
        assertEquals(50.0, result.get("passRate"));
    }

    @Test
    void testAnalyzeCourseWithNoPassOrFail() {
         
        String courseId = "course123";
        List<CourseProgressEvent> mockEvents = List.of(
                createEvent("user1", courseId, CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent("user2", courseId, CourseProgressEvent.EventType.COURSE_STARTED)
        );
        when(repository.findByCourseId(courseId)).thenReturn(mockEvents);

        ResponseEntity<Map<String, Object>> res = service.analyzeCourse(courseId);
        var result = Objects.requireNonNull(res.getBody());

        System.out.println("result::::" + result.entrySet());
        // Asserttions
        assertEquals(2, result.get("participantsStarted"));
        assertEquals(0, result.get("participantsPassed"));
        assertEquals(0, result.get("participantsFailed"));
        assertEquals(0.0, result.get("passRate"));
    }

    @Test
    void testAnalyzeCourseWithNoEvents() {
         
        String courseId = "course123";
        when(repository.findByCourseId(courseId)).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> res = service.analyzeCourse(courseId);

        var result = Objects.requireNonNull(res.getBody());
        System.out.println("result::::" + result.entrySet());

        // Asserttions
        assertEquals(0, result.get("participantsStarted"));
        assertEquals(0, result.get("participantsPassed"));
        assertEquals(0, result.get("participantsFailed"));
        assertEquals(0.0, result.get("passRate"));
    }

    @Test
    void testCreateEventSuccess() {
         
        CourseProgressEvent event = createEvent("user1", "course1", CourseProgressEvent.EventType.COURSE_STARTED);
        ResponseEntity<String> response = service.createEvent(event);

        // Asserttions
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Event created", response.getBody());
        verify(repository, times(1)).save(event);
    }

    @Test
    void testCreateEventValidationFailure() {
         
        CourseProgressEvent event = new CourseProgressEvent(); // Missing required fields
        ResponseEntity<String> response = service.createEvent(event);

        // Asserttions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid event data", response.getBody());
        verify(repository, never()).save(any());
    }

    @Test
    void testGetEventsByUserSuccess() {
         
        String userId = "user1";
        List<CourseProgressEvent> mockEvents = List.of(
                createEvent(userId, "course1", CourseProgressEvent.EventType.COURSE_STARTED),
                createEvent(userId, "course2", CourseProgressEvent.EventType.COURSE_PASSED)
        );
        when(repository.findByUserIdOrderByTimestamp(userId)).thenReturn(mockEvents);

        
        ResponseEntity<List<CourseProgressEvent>> response = service.getEventsByUser(userId);

        // Asserttions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(repository, times(1)).findByUserIdOrderByTimestamp(userId);
    }

    @Test
    void testGetEventsByUserNoEvents() {
         
        String userId = "user1";
        when(repository.findByUserIdOrderByTimestamp(userId)).thenReturn(List.of());

        
        ResponseEntity<List<CourseProgressEvent>> response = service.getEventsByUser(userId);

        // Asserttions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(repository, times(1)).findByUserIdOrderByTimestamp(userId);
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