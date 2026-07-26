package com.interviewiq.repository;

import com.interviewiq.model.InterviewTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewTemplateRepository extends MongoRepository<InterviewTemplate, String> {
    Optional<InterviewTemplate> findByCompany(String company);
}
