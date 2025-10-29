package com.user.validate.controller;

import com.user.validate.model.CourseProgressEvent;
import com.user.validate.repository.CourseProgressEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * This class handles RESTful endpoints for managing and analyzing
 * course progress events. It provides functionality to:
 * - Create and validate course progress events.
 * - Retrieve all events for a specific user in chronological order.
 * - Analyze course metrics, including participant counts and pass rates.
 *
 * This controller interacts with the CourseProgressEventRepository to perform
 * database operations and ensures proper validation and error handling.
 */
@RestController
@RequestMapping("/v1/events")
public class CourseProgressController {

    @Autowired
    private final CourseProgressEventRepository repository;

    public CourseProgressController(CourseProgressEventRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<String> createEvent(@RequestBody CourseProgressEvent event) {
        if (event.getUserId() == null || event.getCourseId() == null || event.getTimestamp() == null || event.getEventType() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid event data");
        }
        repository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body("Event created");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CourseProgressEvent>> getEventsByUser(@PathVariable String userId) {
        List<CourseProgressEvent> userEvents = repository.findByUserIdOrderByTimestamp(userId);
        return ResponseEntity.ok(userEvents);
    }

    @GetMapping("/analysis/course/{courseId}")
    public ResponseEntity<Map<String, Object>> analyzeCourse(@PathVariable String courseId) {
        List<CourseProgressEvent> courseEvents = repository.findByCourseId(courseId);

        Set<String> startedUsers = new HashSet<>();
        Set<String> passedUsers = new HashSet<>();
        Set<String> failedUsers = new HashSet<>();

        for (CourseProgressEvent event : courseEvents) {
            switch (event.getEventType()) {
                case COURSE_STARTED -> startedUsers.add(event.getUserId());
                case COURSE_PASSED -> passedUsers.add(event.getUserId());
                case COURSE_FAILED -> failedUsers.add(event.getUserId());
            }
        }

        int participantsStarted = startedUsers.size();
        int participantsPassed = passedUsers.size();
        int participantsFailed = failedUsers.size();
        double passRate = participantsPassed + participantsFailed > 0
                ? (participantsPassed * 100.0) / (participantsPassed + participantsFailed)
                : 0.0;

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("participantsStarted", participantsStarted);
        analysis.put("participantsPassed", participantsPassed);
        analysis.put("participantsFailed", participantsFailed);
        analysis.put("passRate", passRate);

        return ResponseEntity.ok(analysis);
    }
}