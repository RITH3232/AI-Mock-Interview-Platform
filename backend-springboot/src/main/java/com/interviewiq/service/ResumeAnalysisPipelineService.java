package com.interviewiq.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.model.Resume;
import com.interviewiq.model.ResumeAnalysis;
import com.interviewiq.repository.ResumeAnalysisRepository;
import com.interviewiq.repository.ResumeRepository;
import com.interviewiq.service.prompts.Prompts;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ResumeAnalysisPipelineService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Autowired
    private OpenAiService openAiService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Async
    public void runPipeline(String resumeId, byte[] pdfBytes) {
        try {
            log.info("Starting background analysis for Resume ID: {}", resumeId);

            // 1. Mark as analyzing
            updateResumeStatus(resumeId, "analyzing");

            // 2. Parse PDF using PDFBox
            log.info("Parsing PDF buffer for Resume ID: {}", resumeId);
            String rawText;
            try {
                rawText = extractTextFromPdf(pdfBytes);
                log.info("Successfully parsed PDF. Extracted text length: {} chars", rawText.length());
            } catch (Exception e) {
                log.error("Failed to parse PDF document for Resume ID: {}", resumeId, e);
                updateResumeStatus(resumeId, "failed");
                return;
            }

            // Save raw text to Resume doc
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + resumeId));
            resume.setRawText(rawText);
            resumeRepository.save(resume);

            // 3. Prompt AI
            String prompt = Prompts.RESUME_ANALYSIS_PROMPT.replace("{{RESUME_TEXT}}", rawText);
            log.info("Sending prompt to OpenAI for Resume Analysis.");
            String aiResult = openAiService.analyze(prompt);

            // 4. Save Analysis Report
            log.info("Saving Resume Analysis report to DB.");
            ResumeAnalysis analysis = objectMapper.readValue(aiResult, ResumeAnalysis.class);
            analysis.setResumeId(resumeId);
            
            // Delete previous analysis if any
            resumeAnalysisRepository.findByResumeId(resumeId)
                    .ifPresent(existing -> resumeAnalysisRepository.delete(existing));
            
            resumeAnalysisRepository.save(analysis);

            // 5. Mark as completed
            updateResumeStatus(resumeId, "completed");
            log.info("Background analysis completed successfully for Resume ID: {}", resumeId);

        } catch (Exception e) {
            log.error("Resume analysis pipeline failed for Resume ID: {}", resumeId, e);
            updateResumeStatus(resumeId, "failed");
        }
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private void updateResumeStatus(String resumeId, String status) {
        resumeRepository.findById(resumeId).ifPresent(resume -> {
            resume.setStatus(status);
            resumeRepository.save(resume);
            log.info("Updated status of Resume {} to {}", resumeId, status);
        });
    }
}
