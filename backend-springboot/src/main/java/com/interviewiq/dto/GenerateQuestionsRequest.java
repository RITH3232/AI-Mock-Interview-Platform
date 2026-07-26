package com.interviewiq.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GenerateQuestionsRequest {
    /** @deprecated superseded by {@link #role}; kept for backward compatibility with older clients. */
    @Deprecated
    private String domain;

    @NotBlank(message = "Difficulty is required")
    private String difficulty; // "Easy" | "Medium" | "Hard"

    @NotBlank(message = "Experience level is required")
    private String experienceLevel; // "Fresher" | "0-1 Years" | "2-4 Years" | "5+ Years"

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 20, message = "Count cannot exceed 20")
    private Integer count = 5;

    @NotBlank(message = "Interview type is required")
    private String type; // "Technical" | "HR" | "Behavioral" | "System Design" | "Mixed" | "Aptitude"

    private String company; // optional, can be empty or null

    // ---- Personalized setup fields ----

    /** The role the candidate is interviewing for, e.g. "Java Developer". Takes precedence over {@link #domain}. */
    private String role;

    /** Multi-select skill chips, e.g. ["Java", "Spring Boot", "SQL"]. */
    private List<String> skills = new ArrayList<>();

    /** "Startup" | "Product Company" | "Service Company" | "FAANG Style" */
    private String companyType;

    /** Optional specific target company, overrides companyType styling when set. */
    private String targetCompany;

    /** Effective role: prefers {@link #role}, falls back to legacy {@link #domain}. */
    public String getEffectiveRole() {
        if (role != null && !role.isBlank()) {
            return role;
        }
        return domain;
    }
}
