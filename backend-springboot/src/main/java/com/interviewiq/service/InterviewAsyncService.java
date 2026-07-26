package com.interviewiq.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.model.InterviewSession;
import com.interviewiq.model.Question;
import com.interviewiq.model.ResumeAnalysis;
import com.interviewiq.repository.InterviewSessionRepository;
import com.interviewiq.repository.QuestionRepository;
import com.interviewiq.repository.ResumeAnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InterviewAsyncService {

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Autowired
    private PromptBuilderService promptBuilderService;

    @Autowired
    private OpenAiService openAiService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Async
    public void generatePersonalizedQuestionsAsync(
            String sessionId,
            String role,
            List<String> skills,
            String difficulty,
            String experienceLevel,
            String interviewType,
            int count,
            String companyType,
            String targetCompany
    ) {
        try {
            log.info("Starting personalized question generation for Session ID: {}, Role: {}", sessionId, role);

            String prompt = promptBuilderService.buildPersonalizedPrompt(
                    role, skills, difficulty, experienceLevel, interviewType, count, companyType, targetCompany
            );

            Map<String, Object> opts = new HashMap<>();
            opts.put("domain", role);
            opts.put("interviewType", interviewType);

            String aiResult = openAiService.analyze(prompt, 3, opts);
            List<Question> aiQuestions = parseAiQuestions(aiResult, sessionId, role, difficulty, experienceLevel, targetCompany, "ai", interviewType);

            List<Question> savedQuestions = questionRepository.saveAll(aiQuestions);
            List<String> questionIds = savedQuestions.stream().map(Question::getId).collect(Collectors.toList());

            updateSessionReady(sessionId, questionIds);
            log.info("Personalized session ID: {} is now READY with {} questions.", sessionId, savedQuestions.size());

        } catch (Exception e) {
            log.error("Failed to generate personalized questions asynchronously for Session ID: {}", sessionId, e);
            updateSessionFailed(sessionId, e);
        }
    }

    @Async
    public void generateResumeQuestionsAsync(String sessionId, String resumeId, String difficulty, String experienceLevel, int count) {
        try {
            log.info("Starting background resume question generation for Session ID: {}", sessionId);

            ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(resumeId)
                    .orElseThrow(() -> new IllegalArgumentException("Resume analysis not found for Resume ID: " + resumeId));

            List<String> projectStrings = analysis.getExtractedProjects().stream()
                    .map(p -> p.getName() + " (using " + String.join(", ", p.getTechnologies()) + ")")
                    .collect(Collectors.toList());

            String prompt = promptBuilderService.buildResumePrompt(
                    analysis.getExtractedSkills(),
                    projectStrings,
                    analysis.getExtractedExperience(),
                    analysis.getExtractedEducation(),
                    analysis.getExtractedCertifications(),
                    difficulty,
                    count
            );

            String aiResult = openAiService.analyze(prompt);
            List<Question> aiQuestions = parseAiQuestions(aiResult, sessionId, "Mixed", difficulty, experienceLevel, null, "resume", "Resume Based");

            List<Question> savedQuestions = questionRepository.saveAll(aiQuestions);
            List<String> questionIds = savedQuestions.stream().map(Question::getId).collect(Collectors.toList());

            updateSessionReady(sessionId, questionIds);
            log.info("Resume session ID: {} is now READY with {} questions.", sessionId, savedQuestions.size());

        } catch (Exception e) {
            log.error("Failed to generate resume questions asynchronously for Session ID: {}", sessionId, e);
            updateSessionFailed(sessionId, e);
        }
    }

    @Async
    public void generateCompanyQuestionsAsync(String sessionId, String company, String domain, String experienceLevel, int count, String interviewType) {
        try {
            log.info("Starting company question generation for Session ID: {}, Company: {}", sessionId, company);

            String prompt = promptBuilderService.buildCompanyPrompt(company, domain, experienceLevel, count, interviewType);

            Map<String, Object> opts = new HashMap<>();
            opts.put("domain", domain);
            opts.put("interviewType", interviewType);

            String aiResult = openAiService.analyze(prompt, 3, opts);
            List<Question> aiQuestions = parseAiQuestions(aiResult, sessionId, domain, "Hard", experienceLevel, company, "company", interviewType);

            List<Question> savedQuestions = questionRepository.saveAll(aiQuestions);
            List<String> questionIds = savedQuestions.stream().map(Question::getId).collect(Collectors.toList());

            updateSessionReady(sessionId, questionIds);
            log.info("Company session ID: {} is now READY with {} questions.", sessionId, savedQuestions.size());

        } catch (Exception e) {
            log.error("Failed to generate company questions asynchronously for Session ID: {}", sessionId, e);
            updateSessionFailed(sessionId, e);
        }
    }

    private void updateSessionReady(String sessionId, List<String> questionIds) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setStatus("ready");
            session.setGeneratedQuestions(questionIds);
            sessionRepository.save(session);
        });
    }

    private void updateSessionFailed(String sessionId, Exception e) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setStatus("cancelled");
            session.setErrorMessage(e.getMessage() != null ? e.getMessage() : "AI response invalid or network request failed");
            
            // Build simple stack trace string
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append(ste.toString()).append("\n");
            }
            session.setErrorDetails(sb.toString());
            
            sessionRepository.save(session);
        });
    }

    private String determineCategory(String domain, String category, String interviewType) {
        if ("HR".equalsIgnoreCase(interviewType) || "Behavioral".equalsIgnoreCase(interviewType)
                || "Behavioral".equalsIgnoreCase(category) || "Behavioral".equalsIgnoreCase(domain)) {
            return "Behavioral";
        }
        if ("DSA".equalsIgnoreCase(domain)) {
            return "DSA";
        }
        List<String> aptitudeDomains = List.of("Aptitude", "Mixed Aptitude", "Quantitative Aptitude", "Logical Reasoning", "Verbal Ability", "Data Interpretation", "Puzzles");
        if ("Aptitude".equalsIgnoreCase(interviewType) || "Aptitude".equalsIgnoreCase(domain) || aptitudeDomains.contains(domain)) {
            return "Aptitude";
        }
        return category != null && !category.isEmpty() ? category : "Technical Theory";
    }

    private List<Question> parseAiQuestions(String aiResult, String sessionId, String domain, String difficulty, String experienceLevel, String company, String source, String interviewType) {
        List<Question> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(aiResult);
            JsonNode questionsNode = root.path("questions");
            if (!questionsNode.isArray()) {
                if (root.isArray()) {
                    questionsNode = root;
                } else {
                    throw new IOException("JSON response does not contain a questions array.");
                }
            }

            for (JsonNode qNode : questionsNode) {
                String questionText = qNode.path("question").asText();
                String category = qNode.path("category").asText("");
                String qDomain = qNode.path("domain").asText(domain);
                String qDifficulty = qNode.path("difficulty").asText(difficulty);
                String qFormat = qNode.path("questionFormat").asText("text");

                String finalCategory = determineCategory(qDomain, category, interviewType);

                Question.QuestionBuilder builder = Question.builder()
                        .sessionId(sessionId)
                        .question(questionText)
                        .category(finalCategory)
                        .domain(domain)
                        .difficulty(finalCategory.equals("Behavioral") ? "Medium" : qDifficulty)
                        .experienceLevel(experienceLevel)
                        .company(company)
                        .type("primary")
                        .source(source)
                        .qualityScore(qNode.path("qualityScore").asDouble(85.0))
                        .questionFormat(finalCategory.equals("Behavioral") ? "text" : qFormat);

                List<String> topics = new ArrayList<>();
                qNode.path("expectedTopics").forEach(n -> topics.add(n.asText()));
                builder.expectedTopics(topics);

                List<String> criteria = new ArrayList<>();
                qNode.path("evaluationCriteria").forEach(n -> criteria.add(n.asText()));
                builder.evaluationCriteria(criteria);

                List<String> hints = new ArrayList<>();
                qNode.path("hints").forEach(n -> hints.add(n.asText()));
                builder.hints(hints);

                List<String> tags = new ArrayList<>();
                qNode.path("tags").forEach(n -> tags.add(n.asText()));
                if (company != null && !company.isEmpty()) {
                    tags.add(company.toLowerCase());
                }
                builder.tags(tags);

                if ("mcq".equals(qFormat)) {
                    List<String> options = new ArrayList<>();
                    qNode.path("options").forEach(n -> options.add(n.asText()));
                    builder.options(options);
                    builder.correctOption(qNode.path("correctOption").asText());
                }

                if ("code".equals(qFormat)) {
                    builder.starterCode(qNode.path("starterCode").asText());
                    List<String> langs = new ArrayList<>();
                    qNode.path("languageOptions").forEach(n -> langs.add(n.asText()));
                    if (!langs.isEmpty()) {
                        builder.languageOptions(langs);
                    }

                    List<Question.TestCase> testCases = new ArrayList<>();
                    JsonNode tcNodes = qNode.path("testCases");
                    if (tcNodes.isArray()) {
                        for (JsonNode tcNode : tcNodes) {
                            testCases.add(Question.TestCase.builder()
                                    .input(tcNode.path("input").asText())
                                    .expectedOutput(tcNode.path("expectedOutput").asText())
                                    .isHidden(tcNode.path("isHidden").asBoolean(false))
                                    .build());
                        }
                    }
                    builder.testCases(testCases);
                }

                list.add(builder.build());
            }
        } catch (Exception e) {
            log.error("Failed to parse AI questions JSON output: \n{}", aiResult, e);
            throw new RuntimeException("AI JSON parsing failed", e);
        }
        return list;
    }
}
