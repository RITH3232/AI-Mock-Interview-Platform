package com.interviewiq.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.exception.AppException;
import com.interviewiq.model.Answer;
import com.interviewiq.model.AnswerEvaluation;
import com.interviewiq.model.InterviewReport;
import com.interviewiq.model.InterviewSession;
import com.interviewiq.repository.AnswerEvaluationRepository;
import com.interviewiq.repository.AnswerRepository;
import com.interviewiq.repository.InterviewReportRepository;
import com.interviewiq.repository.InterviewSessionRepository;
import com.interviewiq.service.prompts.Prompts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InterviewReportService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private AnswerEvaluationRepository evaluationRepository;

    @Autowired
    private InterviewReportRepository reportRepository;

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private GamificationService gamificationService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Transactional
    public InterviewReport generateReport(String sessionId) {
        log.info("Generating performance report for Session ID: {}", sessionId);

        // 1. Fetch all answers for the session
        List<Answer> answers = answerRepository.findByInterviewSessionId(sessionId);
        List<String> answerIds = answers.stream().map(Answer::getId).collect(Collectors.toList());

        // 2. Poll for evaluations to complete (as they run asynchronously in the background)
        List<AnswerEvaluation> evals = new ArrayList<>();
        long maxWaitMs = 30000;
        long pollIntervalMs = 2000;
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < maxWaitMs) {
            evals = evaluationRepository.findByAnswerIdIn(answerIds);
            if (evals.size() >= answers.size()) {
                break;
            }
            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AppException("Background evaluation polling interrupted", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // If still no evaluations after timeout, use synthetic ones
        if (evals.isEmpty() && !answers.isEmpty()) {
            log.warn("Background evaluation timed out for Session ID: {}. Generating synthetic scores.", sessionId);
            for (Answer a : answers) {
                evals.add(AnswerEvaluation.builder()
                        .answerId(a.getId())
                        .technicalScore(50.0)
                        .communicationScore(50.0)
                        .completenessScore(50.0)
                        .confidenceScore(50.0)
                        .problemSolvingScore(50.0)
                        .domainKnowledgeScore(50.0)
                        .overallScore(50.0)
                        .strengths(new ArrayList<>())
                        .weaknesses(List.of("Evaluation timed out"))
                        .missingConcepts(new ArrayList<>())
                        .recommendations(List.of("Please retry the interview for accurate scores"))
                        .build());
            }
        }

        if (evals.isEmpty()) {
            throw new AppException("No answers found for this session to generate report", HttpStatus.BAD_REQUEST);
        }

        // 3. Aggregate scores
        double tTech = 0, tComm = 0, tConf = 0, tPS = 0, tDom = 0, tOverall = 0;
        Set<String> allWeaknesses = new HashSet<>();
        Set<String> allMissing = new HashSet<>();
        Set<String> allStrengths = new HashSet<>();
        List<String> allRecommendations = new ArrayList<>();

        for (AnswerEvaluation e : evals) {
            tTech += e.getTechnicalScore();
            tComm += e.getCommunicationScore();
            tConf += e.getConfidenceScore();
            tPS += e.getProblemSolvingScore();
            tDom += e.getDomainKnowledgeScore() != null ? e.getDomainKnowledgeScore() : 0.0;
            tOverall += e.getOverallScore();

            if (e.getWeaknesses() != null) allWeaknesses.addAll(e.getWeaknesses());
            if (e.getMissingConcepts() != null) allMissing.addAll(e.getMissingConcepts());
            if (e.getStrengths() != null) allStrengths.addAll(e.getStrengths());
            if (e.getRecommendations() != null) allRecommendations.addAll(e.getRecommendations());
        }

        int count = evals.size();
        double avgTech = Math.round(tTech / count);
        double avgComm = Math.round(tComm / count);
        double avgConf = Math.round(tConf / count);
        double avgPS = Math.round(tPS / count);
        double avgDom = Math.round(tDom / count);
        double avgOverall = Math.round(tOverall / count);

        // 4. Generate Learning Roadmap
        List<String> limitedWeaknesses = allWeaknesses.stream().limit(10).collect(Collectors.toList());
        List<String> limitedMissing = allMissing.stream().limit(10).collect(Collectors.toList());
        
        String roadmapPrompt = Prompts.ROADMAP_PROMPT
                .replace("{{WEAKNESSES}}", !limitedWeaknesses.isEmpty() ? String.join(", ", limitedWeaknesses) : "None")
                .replace("{{MISSING_CONCEPTS}}", !limitedMissing.isEmpty() ? String.join(", ", limitedMissing) : "None");

        List<String> learningRoadmap = new ArrayList<>();
        try {
            String aiRoadmap = openAiService.analyze(roadmapPrompt);
            JsonNode rmNode = objectMapper.readTree(aiRoadmap).path("learningRoadmap");
            if (rmNode.isArray()) {
                rmNode.forEach(n -> learningRoadmap.add(n.asText()));
            }
        } catch (Exception e) {
            log.error("Failed to parse learning roadmap from AI response", e);
        }

        // 4.5. Generate Career Readiness
        InterviewSession sessionDetails = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));

        String readinessPrompt = Prompts.CAREER_READINESS_PROMPT
                .replace("{{OVERALL_SCORE}}", String.valueOf((int) avgOverall))
                .replace("{{TECHNICAL_SCORE}}", String.valueOf((int) avgTech))
                .replace("{{PROBLEM_SOLVING_SCORE}}", String.valueOf((int) avgPS))
                .replace("{{DOMAIN_SCORE}}", String.valueOf((int) avgDom))
                .replace("{{EXPERIENCE_LEVEL}}", sessionDetails.getExperienceLevel() != null ? sessionDetails.getExperienceLevel() : "Fresher");

        String careerReadinessLevel = "Beginner";
        try {
            String aiReadiness = openAiService.analyze(readinessPrompt);
            careerReadinessLevel = objectMapper.readTree(aiReadiness).path("careerReadinessLevel").asText("Beginner");
        } catch (Exception e) {
            log.error("Failed to parse career readiness level from AI response", e);
        }

        // 5. Create Report
        // Delete previous report if exists to prevent duplicates
        reportRepository.findByInterviewSessionId(sessionId).ifPresent(existing -> 
            reportRepository.delete(existing)
        );

        List<String> finalStrengths = allStrengths.stream().limit(5).collect(Collectors.toList());
        List<String> finalWeaknesses = allWeaknesses.stream().limit(5).collect(Collectors.toList());
        List<String> finalRecommendations = allRecommendations.stream().distinct().limit(5).collect(Collectors.toList());
        if (finalRecommendations.isEmpty()) {
            finalRecommendations = List.of("Review missing concepts.", "Practice mock interviews to improve confidence.");
        }

        InterviewReport report = InterviewReport.builder()
                .interviewSessionId(sessionId)
                .overallScore(avgOverall)
                .technicalScore(avgTech)
                .communicationScore(avgComm)
                .confidenceScore(avgConf)
                .problemSolvingScore(avgPS)
                .domainScore(avgDom)
                .careerReadinessLevel(careerReadinessLevel)
                .strengths(finalStrengths)
                .weaknesses(finalWeaknesses)
                .recommendations(finalRecommendations)
                .learningRoadmap(learningRoadmap)
                .build();

        InterviewReport savedReport = reportRepository.save(report);

        // 6. Close session
        sessionDetails.setStatus("completed");
        sessionDetails.setScore(avgOverall);
        sessionDetails.setCompletedAt(Instant.now());
        sessionRepository.save(sessionDetails);

        // 7. Trigger Gamification
        try {
            gamificationService.processSessionCompletion(
                    sessionDetails.getUserId(),
                    sessionId,
                    avgOverall,
                    sessionDetails.getCompany(),
                    sessionDetails.getDomain()
            );
        } catch (Exception e) {
            log.error("Gamification updates failed for Session ID: {}", sessionId, e);
        }

        return savedReport;
    }
}
