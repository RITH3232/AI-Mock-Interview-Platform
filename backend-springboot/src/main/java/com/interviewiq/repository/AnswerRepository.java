package com.interviewiq.repository;

import com.interviewiq.model.Answer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends MongoRepository<Answer, String> {
    List<Answer> findByInterviewSessionId(String interviewSessionId);
}
