package com.interviewiq.controller;

import com.interviewiq.dto.ApiResponse;
import com.interviewiq.exception.AppException;
import com.interviewiq.model.Resume;
import com.interviewiq.model.ResumeAnalysis;
import com.interviewiq.repository.ResumeAnalysisRepository;
import com.interviewiq.repository.ResumeRepository;
import com.interviewiq.security.UserPrincipal;
import com.interviewiq.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Resume>>> uploadResume(
            @RequestParam("resume") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Received resume upload request for user: {}", userPrincipal.getId());
        
        if (file.isEmpty()) {
            throw new AppException("No file provided", HttpStatus.BAD_REQUEST);
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new AppException("Only PDF files are allowed", HttpStatus.BAD_REQUEST);
        }

        try {
            Resume resume = resumeService.uploadAndAnalyze(userPrincipal.getId(), file.getOriginalFilename(), file.getBytes());
            return new ResponseEntity<>(ApiResponse.success(Map.of("resume", resume)), HttpStatus.CREATED);
        } catch (IOException e) {
            log.error("Failed to read uploaded file bytes", e);
            throw new AppException("Failed to process uploaded file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Map<String, Resume>>> getLatestResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching latest resume for user: {}", userPrincipal.getId());
        Resume resume = resumeRepository.findFirstByUserIdOrderByUploadedAtDesc(userPrincipal.getId())
                .orElseThrow(() -> new AppException("No resumes found", HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success(Map.of("resume", resume)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Resume>>> getResumeById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching resume {} for user: {}", id, userPrincipal.getId());
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new AppException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUserId().equals(userPrincipal.getId())) {
            throw new AppException("Resume not found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of("resume", resume)));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getResumeStatus(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching status of resume {} for user: {}", id, userPrincipal.getId());
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new AppException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUserId().equals(userPrincipal.getId())) {
            throw new AppException("Resume not found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of("status", resume.getStatus())));
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<ApiResponse<Map<String, ResumeAnalysis>>> getResumeReport(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Fetching report of resume {} for user: {}", id, userPrincipal.getId());
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new AppException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUserId().equals(userPrincipal.getId())) {
            throw new AppException("Resume not found", HttpStatus.NOT_FOUND);
        }

        ResumeAnalysis report = resumeAnalysisRepository.findByResumeId(resume.getId())
                .orElseThrow(() -> new AppException("Analysis report not found or not completed yet", HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success(Map.of("report", report)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Deleting resume {} for user: {}", id, userPrincipal.getId());
        resumeService.deleteResume(id, userPrincipal.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/analyze/{id}")
    public ResponseEntity<ApiResponse<Void>> analyzeResume(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Received request to re-analyze resume {} for user: {}", id, userPrincipal.getId());
        // Verify ownership
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new AppException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUserId().equals(userPrincipal.getId())) {
            throw new AppException("Resume not found", HttpStatus.NOT_FOUND);
        }

        // Return mock 400 response matching Node.js
        return new ResponseEntity<>(
                ApiResponse.successMessage("Feature to fetch remote buffer and re-analyze is not implemented yet. Please re-upload."),
                HttpStatus.BAD_REQUEST
        );
    }
}
