package com.interviewiq.service;

import com.interviewiq.exception.AppException;
import com.interviewiq.model.Answer;
import com.interviewiq.repository.AnswerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EvaluationService evaluationService;

    @Transactional
    public Answer submitAnswer(
            String sessionId,
            String questionId,
            String answerType,
            Integer duration,
            String answerText,
            String transcript,
            byte[] audioBytes
    ) {
        String audioUrl = null;

        if (audioBytes != null && audioBytes.length > 0) {
            log.info("Uploading voice answer audio file to Cloudinary.");
            try {
                Map result = cloudinaryService.upload(audioBytes, "interview-iq/audio-answers");
                audioUrl = (String) result.get("secure_url");
            } catch (IOException e) {
                log.error("Failed to upload audio to Cloudinary", e);
                throw new AppException("Failed to upload audio answer", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Answer answer = Answer.builder()
                .interviewSessionId(sessionId)
                .questionId(questionId)
                .answerType(answerType)
                .duration(duration)
                .answerText(answerText)
                .transcript(transcript)
                .audioUrl(audioUrl)
                .build();

        Answer savedAnswer = answerRepository.save(answer);

        // Queue background evaluation
        evaluationService.evaluateAnswerAsync(savedAnswer.getId());

        return savedAnswer;
    }
}
