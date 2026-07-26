package com.interviewiq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question {
    @Id
    private String id;

    private String sessionId; // Reference to InterviewSession.id
    private String question;

    @Builder.Default
    private String questionFormat = "text"; // "text" | "code" | "mcq"

    @Builder.Default
    private List<String> options = new ArrayList<>(); // Used for MCQs

    private String correctOption; // Used for MCQs

    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>(); // Used for coding questions

    private String starterCode; // Used for coding questions

    @Builder.Default
    private List<String> languageOptions = new ArrayList<>(List.of("javascript", "python", "java", "cpp"));

    private String category; // "Technical Theory" | "Coding Concepts" | "Behavioral" | "Aptitude" | "Project Discussion" | "Resume Discussion" | "Scenario-Based" | "DSA"
    private String domain;
    private String difficulty; // "Easy" | "Medium" | "Hard"
    private String experienceLevel; // "Fresher" | "Internship" | "1 Year" | "2+ Years"
    private String company;
    private String type; // "primary" | "follow-up"

    @Builder.Default
    private List<String> expectedTopics = new ArrayList<>();

    @Builder.Default
    private List<String> evaluationCriteria = new ArrayList<>();

    @Builder.Default
    private List<String> hints = new ArrayList<>();

    private String source; // "ai" | "question_bank" | "resume" | "company"
    private Double qualityScore;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCase {
        private String input;
        private String expectedOutput;
        @Builder.Default
        private Boolean isHidden = false;
    }
}
