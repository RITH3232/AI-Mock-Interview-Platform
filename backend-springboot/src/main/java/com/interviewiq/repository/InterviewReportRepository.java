package com.interviewiq.repository;

import com.interviewiq.model.InterviewReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewReportRepository extends MongoRepository<InterviewReport, String> {
    Optional<InterviewReport> findByInterviewSessionId(String interviewSessionId);
}
