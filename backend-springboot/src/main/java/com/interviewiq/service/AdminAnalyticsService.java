package com.interviewiq.service;

import com.interviewiq.repository.InterviewReportRepository;
import com.interviewiq.repository.InterviewSessionRepository;
import com.interviewiq.repository.QuestionRepository;
import com.interviewiq.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminAnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private InterviewReportRepository reportRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Object> getPlatformHealth() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsDeletedFalse();
        
        long totalInterviews = sessionRepository.count();
        long completedInterviews = sessionRepository.countByStatus("completed");
        
        long totalReports = reportRepository.count();
        long totalQuestions = questionRepository.count();

        // Calculate platform average score
        double averageScore = 0.0;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group().avg("overallScore").as("avgScore")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "interviewreports", Map.class);
        if (results.getUniqueMappedResult() != null && results.getUniqueMappedResult().containsKey("avgScore")) {
            Object val = results.getUniqueMappedResult().get("avgScore");
            if (val instanceof Number) {
                averageScore = Math.round(((Number) val).doubleValue());
            }
        }

        // Daily Activity mock data for charts
        List<Map<String, Object>> dailyActivity = List.of(
                Map.of("name", "Mon", "interviews", 12),
                Map.of("name", "Tue", "interviews", 19),
                Map.of("name", "Wed", "interviews", 15),
                Map.of("name", "Thu", "interviews", 22),
                Map.of("name", "Fri", "interviews", 30),
                Map.of("name", "Sat", "interviews", 45),
                Map.of("name", "Sun", "interviews", 40)
        );

        return Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "totalInterviews", totalInterviews,
                "completedInterviews", completedInterviews,
                "totalReports", totalReports,
                "totalQuestions", totalQuestions,
                "averageScore", (int) averageScore,
                "openAiUsageEst", totalQuestions * 0.01,
                "dailyActivity", dailyActivity
        );
    }
}
