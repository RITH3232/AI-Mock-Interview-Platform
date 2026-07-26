package com.interviewiq.repository;

import com.interviewiq.model.AnswerEvaluation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerEvaluationRepository extends MongoRepository<AnswerEvaluation, String> {
    Optional<AnswerEvaluation> findByAnswerId(String answerId);
    List<AnswerEvaluation> findByAnswerIdIn(List<String> answerIds);
}
