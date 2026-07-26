package com.interviewiq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateFollowUpRequest {
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotBlank(message = "Question ID is required")
    private String questionId;

    @NotBlank(message = "Candidate answer is required")
    private String candidateAnswer;
}
