package com.user.validate.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class CourseProgressEvent {
    @Id
    @GeneratedValue
    private UUID eventId;
    private String userId;
    private String courseId;
    private Instant timestamp;
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public enum EventType {
        COURSE_STARTED,
        COURSE_PASSED,
        COURSE_FAILED
    }

    // Getters and Setters
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
