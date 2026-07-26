package com.interviewiq.controller;

import com.interviewiq.dto.*;
import com.interviewiq.exception.AppException;
import com.interviewiq.model.InterviewSession;
import com.interviewiq.model.Question;
import com.interviewiq.repository.QuestionRepository;
import com.interviewiq.security.UserPrincipal;
import com.interviewiq.service.InterviewService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/interview")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private QuestionRepository questionRepository;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Map<String, InterviewSession>>> generateQuestions(
            @Valid @RequestBody GenerateQuestionsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Received request to generate personalized questions for user: {}", userPrincipal.getId());
        InterviewSession session = interviewService.generateQuestions(
                userPrincipal.getId(),
                request.getEffectiveRole(),
                request.getDifficulty(),
                request.getExperienceLevel(),
                request.getCount(),
                request.getType(),
                request.getSkills(),
                request.getCompanyType(),
                request.getTargetCompany()
        );

        return new ResponseEntity<>(ApiResponse.success(Map.of("session", session)), HttpStatus.ACCEPTED);
    }

    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<Map<String, InterviewSession>>> generateResumeQuestions(
            @Valid @RequestBody GenerateResumeQuestionsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Received request to generate resume-based questions for user: {}", userPrincipal.getId());
        InterviewSession session = interviewService.generateResumeQuestions(
                userPrincipal.getId(),
                request.getResumeId(),
                request.getDifficulty(),
                request.getExperienceLevel(),
                request.getCount()
        );

        return new ResponseEntity<>(ApiResponse.success(Map.of("session", session)), HttpStatus.ACCEPTED);
    }

    @PostMapping("/company")
    public ResponseEntity<ApiResponse<Map<String, InterviewSession>>> generateCompanyQuestions(
            @Valid @RequestBody GenerateCompanyQuestionsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Received request to generate company-specific questions for user: {}", userPrincipal.getId());
        InterviewSession session = interviewService.generateCompanyQuestions(
                userPrincipal.getId(),
                request.getCompany(),
                request.getDomain(),
                request.getDifficulty(),
                request.getExperienceLevel(),
                request.getCount(),
                request.getType()
        );

        return new ResponseEntity<>(ApiResponse.success(Map.of("session", session)), HttpStatus.ACCEPTED);
    }

    @PostMapping("/followup")
    public ResponseEntity<ApiResponse<Map<String, Question>>> generateFollowUp(
            @Valid @RequestBody GenerateFollowUpRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Received request to generate follow-up question for session: {}", request.getSessionId());
        
        // Ownership verification
        InterviewSession session = interviewService.getSession(request.getSessionId(), userPrincipal.getId());
        
        Question followUp = interviewService.generateFollowUp(
                session.getId(),
                request.getQuestionId(),
                request.getCandidateAnswer()
        );

        return new ResponseEntity<>(ApiResponse.success(Map.of("followUp", followUp)), HttpStatus.CREATED);
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching session details for Session ID: {}", id);
        InterviewSession session = interviewService.getSession(id, userPrincipal.getId());

        // Populate generatedQuestions
        List<Question> questions = questionRepository.findBySessionId(session.getId());

        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("id", session.getId());
        sessionMap.put("_id", session.getId());
        sessionMap.put("userId", session.getUserId());
        sessionMap.put("resumeId", session.getResumeId());
        sessionMap.put("interviewType", session.getInterviewType());
        sessionMap.put("difficulty", session.getDifficulty());
        sessionMap.put("domain", session.getDomain());
        sessionMap.put("experienceLevel", session.getExperienceLevel());
        sessionMap.put("company", session.getCompany());
        sessionMap.put("role", session.getRole());
        sessionMap.put("skills", session.getSkills());
        sessionMap.put("companyType", session.getCompanyType());
        sessionMap.put("targetCompany", session.getTargetCompany());
        sessionMap.put("totalQuestions", session.getTotalQuestions());
        sessionMap.put("generatedQuestions", questions); // Populated questions array
        sessionMap.put("status", session.getStatus());
        sessionMap.put("errorMessage", session.getErrorMessage());
        sessionMap.put("errorDetails", session.getErrorDetails());
        sessionMap.put("score", session.getScore());
        sessionMap.put("duration", session.getDuration());
        sessionMap.put("startedAt", session.getStartedAt());
        sessionMap.put("completedAt", session.getCompletedAt());
        sessionMap.put("createdAt", session.getCreatedAt());
        sessionMap.put("updatedAt", session.getUpdatedAt());

        return ResponseEntity.ok(ApiResponse.success(Map.of("session", sessionMap)));
    }
}
