package com.interviewiq.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateResumeQuestionsRequest {
    @NotBlank(message = "Resume ID is required")
    private String resumeId;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotBlank(message = "Experience level is required")
    private String experienceLevel;

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 20, message = "Count cannot exceed 20")
    private Integer count = 5;
}
