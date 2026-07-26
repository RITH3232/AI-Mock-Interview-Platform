package com.interviewiq.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PromptBuilderServiceTest {

    @Autowired
    private PromptBuilderService promptBuilderService;

    @Test
    public void testBuildPersonalizedPrompt_IncludesRoleSkillsAndRelevanceRules() {
        String prompt = promptBuilderService.buildPersonalizedPrompt(
                "Java Developer",
                List.of("Java", "OOP", "Spring Boot"),
                "Medium",
                "Fresher",
                "Technical",
                10,
                null,
                null
        );

        assertNotNull(prompt);
        assertTrue(prompt.contains("Java Developer"));
        assertTrue(prompt.contains("Java, OOP, Spring Boot"));
        assertTrue(prompt.contains("Medium"));
        assertTrue(prompt.contains("Fresher"));
        assertTrue(prompt.contains("EVERY question MUST be directly derived from the Role and the selected Skills"));
    }

    @Test
    public void testBuildPersonalizedPrompt_TargetCompanyOverridesCompanyContext() {
        String prompt = promptBuilderService.buildPersonalizedPrompt(
                "Backend Developer",
                List.of("Java", "SQL"),
                "Hard",
                "2-4 Years",
                "Technical",
                5,
                "Startup",
                "Google"
        );

        assertTrue(prompt.contains("Target Company: Google"));
        assertTrue(prompt.contains("Algorithm-heavy")); // Google's interviewStyle from companies.json
    }

    @Test
    public void testBuildHRPrompt_SoftwareEngineering() {
        String prompt = promptBuilderService.buildHRPrompt("Software Engineering", "Hard", "2+ Years", 5);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Software Engineering"));
        assertTrue(prompt.contains("Hard"));
        assertTrue(prompt.contains("2+ Years"));
        assertTrue(prompt.contains("implementation approaches")); // Role-specific expectation
        assertTrue(prompt.contains("Do not generate generic HR questions")); // Core instruction
    }

    @Test
    public void testBuildHRPrompt_JavaDeveloper() {
        String prompt = promptBuilderService.buildHRPrompt("Java Developer", "Medium", "Internship", 3);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Java Developer"));
        assertTrue(prompt.contains("Medium"));
        assertTrue(prompt.contains("Internship"));
        assertTrue(prompt.contains("Spring Boot")); // Role-specific expectation
        assertTrue(prompt.contains("Do not generate generic HR questions")); // Core instruction
    }
}
