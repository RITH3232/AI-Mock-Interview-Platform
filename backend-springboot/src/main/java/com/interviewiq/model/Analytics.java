package com.interviewiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "analytics")
public class Analytics {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Reference to User.id

    @Builder.Default
    private Integer totalInterviews = 0;

    @Builder.Default
    private Double averageScore = 0.0;

    @Builder.Default
    private Double bestScore = 0.0;

    @Builder.Default
    private Double worstScore = 0.0;

    @Builder.Default
    private Double technicalAverage = 0.0;

    @Builder.Default
    private Double communicationAverage = 0.0;

    @Builder.Default
    private Double confidenceAverage = 0.0;

    @Builder.Default
    private Double problemSolvingAverage = 0.0;

    @Builder.Default
    private String careerReadinessLevel = "Beginner";

    @Builder.Default
    private Double weeklyAverageScore = 0.0;

    @Builder.Default
    private Double monthlyAverageScore = 0.0;

    @Builder.Default
    private Integer totalXP = 0;

    @Builder.Default
    private Integer totalAchievements = 0;

    @Builder.Default
    private Map<String, Double> domainBreakdown = new HashMap<>();

    @Builder.Default
    private Map<String, Double> companyBreakdown = new HashMap<>();

    @Builder.Default
    private String favoriteDomain = "";

    @Builder.Default
    private String favoriteCompany = "";

    @Builder.Default
    private StreakInfo streakInfo = new StreakInfo();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakInfo {
        @Builder.Default
        private Integer current = 0;

        @Builder.Default
        private Integer longest = 0;

        private Instant lastInterviewDate;
    }
}
