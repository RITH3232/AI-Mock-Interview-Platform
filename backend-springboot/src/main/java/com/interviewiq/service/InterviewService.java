package com.interviewiq.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.exception.AppException;
import com.interviewiq.model.InterviewSession;
import com.interviewiq.model.Question;
import com.interviewiq.repository.InterviewSessionRepository;
import com.interviewiq.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InterviewService {

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PromptBuilderService promptBuilderService;

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private InterviewAsyncService asyncService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Transactional
    public InterviewSession generateQuestions(
            String userId,
            String role,
            String difficulty,
            String experienceLevel,
            int count,
            String interviewType,
            List<String> skills,
            String companyType,
            String targetCompany
    ) {
        InterviewSession session = InterviewSession.builder()
                .userId(userId)
                .interviewType(interviewType)
                .difficulty(difficulty)
                .domain(role) // role mirrored into domain for backward compatibility (dashboard, evaluation prompts)
                .role(role)
                .skills(skills != null ? skills : new ArrayList<>())
                .companyType(companyType)
                .targetCompany(targetCompany)
                .experienceLevel(experienceLevel)
                .totalQuestions(count)
                .status("generating")
                .startedAt(Instant.now())
                .build();

        InterviewSession savedSession = sessionRepository.save(session);

        // Run personalized question generation in the background
        asyncService.generatePersonalizedQuestionsAsync(
                savedSession.getId(), role, session.getSkills(), difficulty, experienceLevel, interviewType, count, companyType, targetCompany
        );

        return savedSession;
    }

    @Transactional
    public InterviewSession generateResumeQuestions(String userId, String resumeId, String difficulty, String experienceLevel, int count) {
        InterviewSession session = InterviewSession.builder()
                .userId(userId)
                .resumeId(resumeId)
                .interviewType("Resume Based")
                .difficulty(difficulty)
                .domain("Mixed")
                .experienceLevel(experienceLevel)
                .totalQuestions(count)
                .status("generating")
                .startedAt(Instant.now())
                .build();

        InterviewSession savedSession = sessionRepository.save(session);

        // Run resume question generation in the background
        asyncService.generateResumeQuestionsAsync(savedSession.getId(), resumeId, difficulty, experienceLevel, count);

        return savedSession;
    }

    @Transactional
    public InterviewSession generateCompanyQuestions(String userId, String company, String domain, String difficulty, String experienceLevel, int count, String interviewType) {
        InterviewSession session = InterviewSession.builder()
                .userId(userId)
                .interviewType(interviewType)
                .difficulty(difficulty)
                .domain(domain)
                .experienceLevel(experienceLevel)
                .company(company)
                .totalQuestions(count)
                .status("generating")
                .startedAt(Instant.now())
                .build();

        InterviewSession savedSession = sessionRepository.save(session);

        // Run company question generation in the background
        asyncService.generateCompanyQuestionsAsync(savedSession.getId(), company, domain, experienceLevel, count, interviewType);

        return savedSession;
    }

    @Transactional
    public Question generateFollowUp(String sessionId, String questionId, String candidateAnswer) {
        Question original = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException("Original question not found", HttpStatus.NOT_FOUND));

        String prompt = promptBuilderService.buildFollowUpPrompt(original.getQuestion());
        String aiResponse = openAiService.analyze(prompt);

        try {
            JsonNode qData = objectMapper.readTree(aiResponse);
            if (qData.isArray() && qData.size() > 0) {
                qData = qData.get(0);
            }

            List<String> topics = new ArrayList<>();
            qData.path("expectedTopics").forEach(n -> topics.add(n.asText()));

            List<String> criteria = new ArrayList<>();
            qData.path("evaluationCriteria").forEach(n -> criteria.add(n.asText()));

            List<String> hints = new ArrayList<>();
            qData.path("hints").forEach(n -> hints.add(n.asText()));

            List<String> tags = new ArrayList<>();
            qData.path("tags").forEach(n -> tags.add(n.asText()));

            Question followUp = Question.builder()
                    .sessionId(sessionId)
                    .question(qData.path("question").asText())
                    .category(qData.path("category").asText(original.getCategory()))
                    .domain(original.getDomain())
                    .difficulty(original.getDifficulty())
                    .experienceLevel(original.getExperienceLevel())
                    .company(original.getCompany())
                    .type("follow-up")
                    .source("ai")
                    .qualityScore(qData.path("qualityScore").asDouble(80.0))
                    .expectedTopics(topics)
                    .evaluationCriteria(criteria)
                    .hints(hints)
                    .tags(!tags.isEmpty() ? tags : original.getTags())
                    .questionFormat("text")
                    .build();

            Question saved = questionRepository.save(followUp);

            // Push follow-up ID to session array
            InterviewSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));
            session.getGeneratedQuestions().add(saved.getId());
            sessionRepository.save(session);

            return saved;
        } catch (Exception e) {
            log.error("Failed to parse follow-up question JSON", e);
            throw new AppException("Failed to generate follow-up question", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public InterviewSession getSession(String sessionId, String userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException("Session not found", HttpStatus.NOT_FOUND));
    }
}
