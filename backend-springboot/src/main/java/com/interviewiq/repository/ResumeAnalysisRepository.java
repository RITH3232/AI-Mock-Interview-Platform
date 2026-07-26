package com.interviewiq.repository;

import com.interviewiq.model.ResumeAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends MongoRepository<ResumeAnalysis, String> {
    Optional<ResumeAnalysis> findByResumeId(String resumeId);
}
