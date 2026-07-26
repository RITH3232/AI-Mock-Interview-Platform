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
@Document(collection = "resumeanalyses")
public class ResumeAnalysis {
    @Id
    private String id;

    @Indexed(unique = true)
    private String resumeId; // Reference to Resume.id

    @Builder.Default
    private List<String> extractedSkills = new ArrayList<>();

    @Builder.Default
    private List<ExtractedProject> extractedProjects = new ArrayList<>();

    @Builder.Default
    private List<String> extractedExperience = new ArrayList<>();

    @Builder.Default
    private List<String> extractedEducation = new ArrayList<>();

    @Builder.Default
    private List<String> extractedCertifications = new ArrayList<>();

    @Builder.Default
    private String resumeSummary = "";

    @Builder.Default
    private Double atsScore = 0.0;

    @Builder.Default
    private Double resumeScore = 0.0;

    @Builder.Default
    private Double industryReadinessScore = 0.0;

    @Builder.Default
    private Double keywordMatchScore = 0.0;

    @Builder.Default
    private Double projectQualityScore = 0.0;

    @Builder.Default
    private Double experienceScore = 0.0;

    @Builder.Default
    private Double technicalSkillScore = 0.0;

    @Builder.Default
    private Double formattingScore = 0.0;

    @Builder.Default
    private List<String> strengths = new ArrayList<>();

    @Builder.Default
    private List<String> weaknesses = new ArrayList<>();

    @Builder.Default
    private List<String> recommendations = new ArrayList<>();

    @Builder.Default
    private List<String> missingSkills = new ArrayList<>();

    @Builder.Default
    private List<String> missingTechnologies = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedProject {
        private String name;
        @Builder.Default
        private List<String> technologies = new ArrayList<>();
        private String complexity;
        private Double readinessScore;
    }
}
