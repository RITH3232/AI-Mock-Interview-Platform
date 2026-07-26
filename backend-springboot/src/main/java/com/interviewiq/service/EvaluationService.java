package com.interviewiq.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.model.Answer;
import com.interviewiq.model.AnswerEvaluation;
import com.interviewiq.model.Question;
import com.interviewiq.repository.AnswerEvaluationRepository;
import com.interviewiq.repository.AnswerRepository;
import com.interviewiq.repository.QuestionRepository;
import com.interviewiq.service.prompts.Prompts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EvaluationService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerEvaluationRepository evaluationRepository;

    @Autowired
    private OpenAiService openAiService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Async
    public void evaluateAnswerAsync(String answerId) {
        try {
            log.info("Starting background evaluation for Answer ID: {}", answerId);

            Answer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));

            Question question = questionRepository.findById(answer.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + answer.getQuestionId()));

            String candidateResponse = "voice".equalsIgnoreCase(answer.getAnswerType()) 
                    ? answer.getTranscript() 
                    : answer.getAnswerText();

            if (candidateResponse == null) {
                candidateResponse = "No answer provided";
            }

            String prompt = Prompts.EVALUATION_PROMPT
                    .replace("{{QUESTION}}", question.getQuestion())
                    .replace("{{ANSWER}}", candidateResponse)
                    .replace("{{DURATION}}", String.valueOf(answer.getDuration()))
                    .replace("{{EXPECTED_TOPICS}}", !question.getExpectedTopics().isEmpty() ? String.join(", ", question.getExpectedTopics()) : "None specified")
                    .replace("{{DOMAIN}}", question.getDomain() != null ? question.getDomain() : "General");

            String aiResult = openAiService.analyze(prompt);
            JsonNode root = objectMapper.readTree(aiResult);

            double tech = root.path("technicalScore").asDouble(0.0);
            double comm = root.path("communicationScore").asDouble(0.0);
            double comp = root.path("completenessScore").asDouble(0.0);
            double conf = root.path("confidenceScore").asDouble(0.0);
            double ps = root.path("problemSolvingScore").asDouble(0.0);
            double dom = root.path("domainKnowledgeScore").asDouble(0.0);

            // Calculate Overall Score
            // Technical Accuracy: 35%, Communication: 20%, Completeness: 15%, Confidence: 10%, Problem Solving: 15%, Domain: 5%
            double overallScoreDouble = (tech * 0.35) + (comm * 0.20) + (comp * 0.15) + (conf * 0.10) + (ps * 0.15) + (dom * 0.05);
            double overallScore = Math.round(overallScoreDouble);

            List<String> strengths = new ArrayList<>();
            root.path("strengths").forEach(n -> strengths.add(n.asText()));

            List<String> weaknesses = new ArrayList<>();
            root.path("weaknesses").forEach(n -> weaknesses.add(n.asText()));

            List<String> missingConcepts = new ArrayList<>();
            root.path("missingConcepts").forEach(n -> missingConcepts.add(n.asText()));

            List<String> recommendations = new ArrayList<>();
            root.path("recommendations").forEach(n -> recommendations.add(n.asText()));

            // Delete previous evaluation if exists to prevent duplicates
            evaluationRepository.findByAnswerId(answerId).ifPresent(existing -> 
                evaluationRepository.delete(existing)
            );

            AnswerEvaluation evaluation = AnswerEvaluation.builder()
                    .answerId(answerId)
                    .technicalScore(tech)
                    .communicationScore(comm)
                    .completenessScore(comp)
                    .confidenceScore(conf)
                    .problemSolvingScore(ps)
                    .domainKnowledgeScore(dom)
                    .overallScore(overallScore)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .missingConcepts(missingConcepts)
                    .recommendations(recommendations)
                    .build();

            evaluationRepository.save(evaluation);
            log.info("Successfully evaluated Answer ID: {} with overall score: {}", answerId, overallScore);

        } catch (Exception e) {
            log.error("Failed to evaluate Answer ID: {}", answerId, e);
        }
    }
}
