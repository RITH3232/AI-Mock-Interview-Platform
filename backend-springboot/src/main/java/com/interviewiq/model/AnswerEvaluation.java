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
@Document(collection = "answerevaluations")
public class AnswerEvaluation {
    @Id
    private String id;

    @Indexed(unique = true)
    private String answerId; // Reference to Answer.id

    private Double technicalScore;
    private Double communicationScore;
    private Double completenessScore;
    private Double confidenceScore;
    private Double problemSolvingScore;
    private Double domainKnowledgeScore;
    private Double overallScore;

    @Builder.Default
    private List<String> strengths = new ArrayList<>();

    @Builder.Default
    private List<String> weaknesses = new ArrayList<>();

    @Builder.Default
    private List<String> missingConcepts = new ArrayList<>();

    @Builder.Default
    private List<String> recommendations = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
