package com.user.validate.repository;

import com.user.validate.model.CourseProgressEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CourseProgressEventRepository extends JpaRepository<CourseProgressEvent, UUID> {
    List<CourseProgressEvent> findByUserIdOrderByTimestamp(String userId);
    List<CourseProgressEvent> findByCourseId(String courseId);
}
