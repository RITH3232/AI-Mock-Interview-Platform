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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "interviewreports")
public class InterviewReport {
    @Id
    private String id;

    @Indexed(unique = true)
    private String interviewSessionId; // Reference to InterviewSession.id

    private Double overallScore;
    private Double technicalScore;
    private Double communicationScore;
    private Double confidenceScore;
    private Double problemSolvingScore;
    private Double domainScore;

    @Builder.Default
    private List<String> strengths = new ArrayList<>();

    @Builder.Default
    private List<String> weaknesses = new ArrayList<>();

    @Builder.Default
    private List<String> recommendations = new ArrayList<>();

    @Builder.Default
    private List<String> learningRoadmap = new ArrayList<>();

    @Builder.Default
    private String careerReadinessLevel = "Beginner"; // "Beginner" | "Developing" | "Interview Ready" | "Strong Candidate" | "Top Performer"

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
