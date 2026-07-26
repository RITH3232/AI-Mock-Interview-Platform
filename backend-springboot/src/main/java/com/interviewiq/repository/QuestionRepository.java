package com.interviewiq.repository;

import com.interviewiq.model.Question;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findBySessionId(String sessionId);

    @Aggregation(pipeline = {
        "{ $match: { domain: ?0, difficulty: ?1, source: 'question_bank' } }",
        "{ $sample: { size: ?2 } }"
    })
    List<Question> findRandomFromBank(String domain, String difficulty, int limit);
}
