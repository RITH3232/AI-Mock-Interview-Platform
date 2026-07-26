package com.interviewiq.service;

import com.interviewiq.model.InterviewTemplate;
import com.interviewiq.model.Question;
import com.interviewiq.repository.InterviewTemplateRepository;
import com.interviewiq.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionBankService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private InterviewTemplateRepository interviewTemplateRepository;

    public List<Question> getQuestionsFromBank(String domain, String difficulty, int limit) {
        return questionRepository.findRandomFromBank(domain, difficulty, limit);
    }

    public Optional<InterviewTemplate> getTemplate(String companyName) {
        return interviewTemplateRepository.findByCompany(companyName);
    }
}
