package com.interviewiq.repository;

import com.interviewiq.model.InterviewSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepository extends MongoRepository<InterviewSession, String> {
    Optional<InterviewSession> findByIdAndUserId(String id, String userId);
    List<InterviewSession> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    long countByStatus(String status);
}
