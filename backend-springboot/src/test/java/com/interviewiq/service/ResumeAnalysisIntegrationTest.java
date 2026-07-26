package com.interviewiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ResumeAnalysisIntegrationTest {

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private PromptBuilderService promptBuilderService;

    @Test
    public void testDynamicResumeAnalysis_JavaDeveloper() throws Exception {
        String resumeText = "Jane Doe\n" +
                "Skills: Java, Spring Boot, MongoDB, Docker, REST API\n" +
                "Projects:\n" +
                "InterviewIQ Portal\n" +
                "Created an AI mock interview system using Java, Spring Boot, MongoDB, and React.\n" +
                "Education:\n" +
                "Bachelor of Science in Computer Science, State University, 2024\n" +
                "Certifications:\n" +
                "AWS Certified Developer Associate";

        String prompt = "extractedSkills and atsScore Resume Text:\n\"\"\"\n" + resumeText + "\n\"\"\"";

        String resultJson = openAiService.analyze(prompt);
        assertNotNull(resultJson);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resultJson);

        // Verify skills are dynamically extracted from content
        assertTrue(root.has("extractedSkills"));
        JsonNode skills = root.get("extractedSkills");
        assertTrue(skills.isArray());
        
        boolean hasJava = false;
        boolean hasSpringBoot = false;
        for (JsonNode s : skills) {
            if ("Java".equalsIgnoreCase(s.asText())) hasJava = true;
            if ("Spring Boot".equalsIgnoreCase(s.asText())) hasSpringBoot = true;
        }
        assertTrue(hasJava, "Should extract Java");
        assertTrue(hasSpringBoot, "Should extract Spring Boot");

        // Verify projects are dynamically extracted
        assertTrue(root.has("extractedProjects"));
        JsonNode projects = root.get("extractedProjects");
        assertTrue(projects.size() > 0);
        assertTrue(projects.get(0).get("name").asText().contains("InterviewIQ"));

        // Verify summary is dynamic
        assertTrue(root.has("resumeSummary"));
        assertTrue(root.get("resumeSummary").asText().contains("Java"));

        // Verify dynamic scores
        assertTrue(root.has("atsScore"));
        double atsScore = root.get("atsScore").asDouble();
        assertTrue(atsScore > 0 && atsScore <= 100);
    }

    @Test
    public void testDynamicResumeAnalysis_ReactDeveloper() throws Exception {
        String resumeText = "John Doe\n" +
                "Skills: React, Redux, TypeScript, CSS, HTML\n" +
                "Projects:\n" +
                "Portfolio Website\n" +
                "Developed a frontend application with React and Redux.\n" +
                "Education:\n" +
                "B.Tech in Information Technology\n" +
                "Certifications:\n" +
                "Google Frontend Developer Certificate";

        String prompt = "extractedSkills and atsScore Resume Text:\n\"\"\"\n" + resumeText + "\n\"\"\"";

        String resultJson = openAiService.analyze(prompt);
        assertNotNull(resultJson);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resultJson);

        // Verify frontend skills are extracted
        JsonNode skills = root.get("extractedSkills");
        boolean hasReact = false;
        boolean hasTypeScript = false;
        for (JsonNode s : skills) {
            if ("React".equalsIgnoreCase(s.asText())) hasReact = true;
            if ("TypeScript".equalsIgnoreCase(s.asText())) hasTypeScript = true;
        }
        assertTrue(hasReact, "Should extract React");
        assertTrue(hasTypeScript, "Should extract TypeScript");

        // Verify project is dynamic
        JsonNode projects = root.get("extractedProjects");
        assertTrue(projects.get(0).get("name").asText().contains("Portfolio"));
    }

    @Test
    public void testDynamicResumeInterviewQuestions() throws Exception {
        // Build prompt from a specific profile
        String prompt = promptBuilderService.buildResumePrompt(
                List.of("Java", "Spring Boot", "SQL"),
                List.of("InterviewIQ (using Java, Spring Boot)"),
                List.of("Backend Intern"),
                List.of("B.S. CS"),
                List.of("AWS Certified"),
                "Medium",
                5
        );

        // Generate questions in mock mode
        String resultJson = openAiService.analyze(prompt);
        assertNotNull(resultJson);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resultJson);

        assertTrue(root.has("questions"));
        JsonNode questions = root.get("questions");
        assertEquals(5, questions.size());

        // Verify distribution and direct references
        boolean hasProjectRef = false;
        boolean hasJavaOrSpringRef = false;
        boolean hasExperienceRef = false;

        for (JsonNode q : questions) {
            String text = q.get("question").asText();
            if (text.contains("InterviewIQ")) hasProjectRef = true;
            if (text.contains("Java") || text.contains("Spring Boot")) hasJavaOrSpringRef = true;
            if (text.contains("Backend Intern")) hasExperienceRef = true;
        }

        assertTrue(hasProjectRef, "Should reference project 'InterviewIQ' directly");
        assertTrue(hasJavaOrSpringRef, "Should reference candidate tech stack");
        assertTrue(hasExperienceRef, "Should reference candidate experience");
    }
}
