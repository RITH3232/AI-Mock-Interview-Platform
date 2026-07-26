package com.interviewiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.dto.GenerateQuestionsRequest;
import com.interviewiq.model.InterviewSession;
import com.interviewiq.repository.QuestionRepository;
import com.interviewiq.repository.UserRepository;
import com.interviewiq.security.JwtAuthenticationFilter;
import com.interviewiq.security.JwtTokenProvider;
import com.interviewiq.security.CustomUserDetailsService;
import com.interviewiq.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import com.interviewiq.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterviewController.class)
@AutoConfigureMockMvc
public class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterviewService interviewService;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter();
        }

        @Bean(name = "customUserDetailsService")
        public CustomUserDetailsService customUserDetailsService() {
            return org.mockito.Mockito.mock(CustomUserDetailsService.class);
        }
    }

    @Test
    public void generateQuestions_Success() throws Exception {
        UserPrincipal principal = new UserPrincipal(
                "userId", 
                "candidate@example.com", 
                "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
        );

        GenerateQuestionsRequest request = new GenerateQuestionsRequest();
        request.setRole("React Developer");
        request.setDifficulty("Medium");
        request.setExperienceLevel("Internship");
        request.setCount(5);
        request.setType("Technical");
        request.setSkills(List.of("React", "JavaScript"));
        request.setCompanyType("Product Company");
        request.setTargetCompany("Google");

        InterviewSession session = InterviewSession.builder()
                .id("sessionId")
                .userId("userId")
                .role("React Developer")
                .domain("React Developer")
                .status("generating")
                .build();

        when(interviewService.generateQuestions(
                anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyList(), any(), any()))
                .thenReturn(session);

        mockMvc.perform(post("/api/v1/interview/generate")
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.session.id").value("sessionId"));
    }

    @Test
    @WithMockUser(username = "candidate@example.com", roles = {"CANDIDATE"})
    public void generateQuestions_ValidationError() throws Exception {
        GenerateQuestionsRequest request = new GenerateQuestionsRequest();
        // Missing required fields (domain, difficulty, etc.)

        mockMvc.perform(post("/api/v1/interview/generate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
