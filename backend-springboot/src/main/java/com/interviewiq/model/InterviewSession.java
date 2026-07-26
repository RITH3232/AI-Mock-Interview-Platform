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
@Document(collection = "interviewsessions")
public class InterviewSession {
    @Id
    private String id;

    private String userId; // Reference to User.id
    private String resumeId; // Reference to Resume.id
    private String interviewType; // "HR" | "Technical" | "Mixed" | "Resume Based" | "Aptitude"
    private String difficulty; // "Easy" | "Medium" | "Hard"
    private String domain;
    private String experienceLevel; // "Fresher" | "Internship" | "1 Year" | "2+ Years"
    private String company;

    // ---- Personalized setup fields ----
    private String role; // e.g. "Java Developer" (also mirrored into domain for backward compatibility)

    @Builder.Default
    private List<String> skills = new ArrayList<>();

    private String companyType; // "Startup" | "Product Company" | "Service Company" | "FAANG Style"
    private String targetCompany;

    @Builder.Default
    private Integer totalQuestions = 5;

    @Builder.Default
    private List<String> generatedQuestions = new ArrayList<>(); // References to Question.id

    @Builder.Default
    private String status = "configuring"; // "configuring" | "generating" | "ready" | "in-progress" | "completed" | "cancelled"

    private String errorMessage;
    private String errorDetails;
    private Double score;
    private Integer duration; // in seconds

    private Instant startedAt;
    private Instant completedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
