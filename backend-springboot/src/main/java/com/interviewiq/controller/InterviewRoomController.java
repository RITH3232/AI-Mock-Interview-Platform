package com.interviewiq.controller;

import com.interviewiq.dto.ApiResponse;
import com.interviewiq.exception.AppException;
import com.interviewiq.model.Answer;
import com.interviewiq.model.InterviewReport;
import com.interviewiq.model.InterviewSession;
import com.interviewiq.model.Question;
import com.interviewiq.repository.AnswerRepository;
import com.interviewiq.repository.InterviewReportRepository;
import com.interviewiq.repository.InterviewSessionRepository;
import com.interviewiq.repository.QuestionRepository;
import com.interviewiq.security.UserPrincipal;
import com.interviewiq.service.AnswerService;
import com.interviewiq.service.InterviewReportService;
import com.interviewiq.service.PdfGeneratorService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/room")
public class InterviewRoomController {

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private InterviewReportRepository reportRepository;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private InterviewReportService reportService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Map<String, InterviewSession>>> startSession(
            @RequestBody StartRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Starting session: {}", request.getId());
        InterviewSession session = sessionRepository.findByIdAndUserId(request.getId(), userPrincipal.getId())
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));

        session.setStatus("in-progress");
        session.setStartedAt(Instant.now());
        InterviewSession updated = sessionRepository.save(session);

        return ResponseEntity.ok(ApiResponse.success(Map.of("session", updated)));
    }

    @PostMapping(value = "/answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Answer>>> submitAnswerMultipart(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("questionId") String questionId,
            @RequestParam("answerType") String answerType,
            @RequestParam("duration") Integer duration,
            @RequestParam(value = "answerText", required = false) String answerText,
            @RequestParam(value = "transcript", required = false) String transcript,
            @RequestParam(value = "audio", required = false) MultipartFile audioFile,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Submitting voice/text answer for session: {}", sessionId);

        // Verify ownership
        sessionRepository.findByIdAndUserId(sessionId, userPrincipal.getId())
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));

        byte[] audioBytes = null;
        if (audioFile != null && !audioFile.isEmpty()) {
            try {
                audioBytes = audioFile.getBytes();
            } catch (IOException e) {
                log.error("Failed to read audio file bytes", e);
                throw new AppException("Failed to read audio file upload", HttpStatus.BAD_REQUEST);
            }
        }

        Answer answer = answerService.submitAnswer(
                sessionId,
                questionId,
                answerType,
                duration,
                answerText,
                transcript,
                audioBytes
        );

        return new ResponseEntity<>(ApiResponse.success(Map.of("answer", answer)), HttpStatus.CREATED);
    }

    @PostMapping(value = "/answer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Answer>>> submitAnswerJson(
            @RequestBody JsonAnswerRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Submitting JSON answer for session: {}", request.getSessionId());

        // Verify ownership
        sessionRepository.findByIdAndUserId(request.getSessionId(), userPrincipal.getId())
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));

        Answer answer = answerService.submitAnswer(
                request.getSessionId(),
                request.getQuestionId(),
                request.getAnswerType(),
                request.getDuration(),
                request.getAnswerText(),
                request.getTranscript(),
                null
        );

        return new ResponseEntity<>(ApiResponse.success(Map.of("answer", answer)), HttpStatus.CREATED);
    }

    @GetMapping("/progress/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProgress(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching progress of Session ID: {}", id);
        InterviewSession session = sessionRepository.findByIdAndUserId(id, userPrincipal.getId())
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));

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
        sessionMap.put("generatedQuestions", questions);
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

    @PostMapping("/report/generate/{id}")
    public ResponseEntity<ApiResponse<Map<String, InterviewReport>>> generateReport(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Report generation request for Session ID: {}", id);
        
        // Verify ownership
        sessionRepository.findByIdAndUserId(id, userPrincipal.getId())
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));

        InterviewReport report = reportService.generateReport(id);

        return new ResponseEntity<>(ApiResponse.success(Map.of("report", report)), HttpStatus.CREATED);
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchReport(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching report for Session ID: {}", id);
        
        // Verify ownership
        sessionRepository.findByIdAndUserId(id, userPrincipal.getId())
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));

        InterviewReport report = reportRepository.findByInterviewSessionId(id)
                .orElseThrow(() -> new AppException("Report not found", HttpStatus.NOT_FOUND));

        // Fetch actual answers populated with question details
        List<Answer> answers = answerRepository.findByInterviewSessionId(id);
        List<Map<String, Object>> populatedAnswers = new ArrayList<>();
        
        for (Answer ans : answers) {
            Question q = questionRepository.findById(ans.getQuestionId()).orElse(null);
            
            Map<String, Object> map = new HashMap<>();
            map.put("_id", ans.getId());
            map.put("id", ans.getId());
            map.put("interviewSessionId", ans.getInterviewSessionId());
            map.put("questionId", q); // Populate nested question details
            map.put("answerText", ans.getAnswerText());
            map.put("transcript", ans.getTranscript());
            map.put("audioUrl", ans.getAudioUrl());
            map.put("answerType", ans.getAnswerType());
            map.put("duration", ans.getDuration());
            map.put("createdAt", ans.getCreatedAt());
            map.put("updatedAt", ans.getUpdatedAt());
            
            populatedAnswers.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("report", report);
        result.put("answers", populatedAnswers);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> exportReportPDF(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Exporting report PDF for Session ID: {}", id);
        
        // Verify ownership
        sessionRepository.findByIdAndUserId(id, userPrincipal.getId())
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));

        InterviewReport report = reportRepository.findByInterviewSessionId(id)
                .orElseThrow(() -> new AppException("Report not found", HttpStatus.NOT_FOUND));

        byte[] pdfBytes = pdfGeneratorService.generateInterviewPDF(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Interview_Report_" + id + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @Data
    public static class StartRequest {
        private String id;
    }

    @Data
    public static class JsonAnswerRequest {
        private String sessionId;
        private String questionId;
        private String answerType;
        private Integer duration;
        private String answerText;
        private String transcript;
    }
}
