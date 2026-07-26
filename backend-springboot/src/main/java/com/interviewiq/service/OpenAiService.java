package com.interviewiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class OpenAiService {

    @Value("${app.ai.base-url:https://api.groq.com/openai/v1/chat/completions}")
    private String baseUrl;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model:llama-3.3-70b-versatile}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String analyze(String prompt) {
        return analyze(prompt, 3, new HashMap<>());
    }

    public String analyze(String prompt, int retries, Map<String, Object> options) {
        boolean isDummyKey = apiKey == null ||
                apiKey.isBlank() ||
                "your_openai_api_key_here".equals(apiKey) ||
                "your_groq_api_key_here".equals(apiKey) ||
                "dummy_key".equals(apiKey);

        if (isDummyKey) {
            log.info("Using Mock AI Response (No valid API key provided)");
            try {
                // Simulate network delay
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return getMockResponse(prompt, options);
        }

        int attempt = 1;
        while (attempt <= retries) {
            try {
                log.info("Sending real request to AI provider (Attempt {}/{})", attempt, retries);

                Map<String, Object> requestBodyMap = new HashMap<>();
                requestBodyMap.put("model", model);

                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", "You are a helpful assistant designed to output JSON."));
                messages.add(Map.of("role", "user", "content", prompt));
                requestBodyMap.put("messages", messages);

                requestBodyMap.put("response_format", Map.of("type", "json_object"));
                requestBodyMap.put("temperature", 0.2);

                String requestBody = objectMapper.writeValueAsString(requestBodyMap);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    return root.path("choices").get(0).path("message").path("content").asText();
                } else {
                    log.error("AI provider request failed with status: {}, response: {}", response.statusCode(), response.body());
                    throw new IOException("HTTP status " + response.statusCode());
                }
            } catch (Exception e) {
                log.error("AI provider error on attempt {}: {}", attempt, e.getMessage());
                if (attempt == retries) {
                    throw new RuntimeException("Failed to analyze prompt after " + retries + " attempts", e);
                }
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
            attempt++;
        }
        throw new RuntimeException("Failed to analyze prompt");
    }

    private String getMockResponse(String prompt, Map<String, Object> options) {
        try {
            if (prompt.contains("careerReadinessLevel")) {
                int score = 70;
                Pattern pattern = Pattern.compile("Overall Score:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(prompt);
                if (matcher.find()) {
                    score = Integer.parseInt(matcher.group(1));
                }

                String level = "Developing";
                if (score >= 90) level = "Top Performer";
                else if (score >= 80) level = "Strong Candidate";
                else if (score >= 65) level = "Interview Ready";
                else if (score >= 45) level = "Developing";
                else level = "Beginner";

                return objectMapper.writeValueAsString(Map.of("careerReadinessLevel", level));

            } else if (prompt.contains("technicalScore") && prompt.contains("communicationScore")) {
                // Parse expected parameters
                String questionText = "";
                String candidateAnswer = "";
                List<String> expectedTopics = new ArrayList<>();
                String domainText = "General";

                Pattern qPat = Pattern.compile("Question:\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
                Matcher qMat = qPat.matcher(prompt);
                if (qMat.find()) questionText = qMat.group(1).trim();

                Pattern ansPat = Pattern.compile("Candidate's Answer:\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
                Matcher ansMat = ansPat.matcher(prompt);
                if (ansMat.find()) candidateAnswer = ansMat.group(1).trim();

                Pattern topicsPat = Pattern.compile("expected topics:\\s*([^.\\n]+)", Pattern.CASE_INSENSITIVE);
                Matcher topicsMat = topicsPat.matcher(prompt);
                if (topicsMat.find()) {
                    for (String t : topicsMat.group(1).split(",")) {
                        if (!t.trim().isEmpty()) expectedTopics.add(t.trim());
                    }
                }

                Pattern domPat = Pattern.compile("specific to\\s*([^.\\n]+)", Pattern.CASE_INSENSITIVE);
                Matcher domMat = domPat.matcher(prompt);
                if (domMat.find()) domainText = domMat.group(1).trim();

                // Mock MCQ answers
                Map<String, String> mcqAnswers = getMockMcqAnswers();
                String matchedMCQAns = null;
                for (String key : mcqAnswers.keySet()) {
                    if (questionText.toLowerCase().contains(key.toLowerCase())) {
                        matchedMCQAns = mcqAnswers.get(key);
                        break;
                    }
                }

                boolean isPoor = candidateAnswer.isEmpty() ||
                        (matchedMCQAns == null && candidateAnswer.length() < 10) ||
                        isPoorAnswer(candidateAnswer);

                Map<String, Object> result = new HashMap<>();
                Random random = new Random();

                if (isPoor) {
                    result.put("technicalScore", 10 + random.nextInt(10));
                    result.put("communicationScore", 20 + random.nextInt(10));
                    result.put("completenessScore", 10 + random.nextInt(10));
                    result.put("confidenceScore", 10 + random.nextInt(10));
                    result.put("problemSolvingScore", 15 + random.nextInt(10));
                    result.put("domainKnowledgeScore", 10 + random.nextInt(10));
                    result.put("strengths", new ArrayList<>());
                    result.put("weaknesses", List.of("Candidate did not provide a valid answer.", "Lacks basic understanding of the topic."));
                    result.put("missingConcepts", !expectedTopics.isEmpty() ? expectedTopics : List.of("Core concepts"));
                    result.put("recommendations", List.of("Study the fundamentals of " + domainText + ".", "Practice answering questions with best effort."));
                    return objectMapper.writeValueAsString(result);
                }

                if (matchedMCQAns != null) {
                    boolean isCorrect = candidateAnswer.toLowerCase().contains(matchedMCQAns.toLowerCase()) ||
                            matchedMCQAns.toLowerCase().contains(candidateAnswer.toLowerCase());

                    if (isCorrect) {
                        result.put("technicalScore", 95 + random.nextInt(5));
                        result.put("communicationScore", 90 + random.nextInt(10));
                        result.put("completenessScore", 100);
                        result.put("confidenceScore", 95 + random.nextInt(5));
                        result.put("problemSolvingScore", 90 + random.nextInt(10));
                        result.put("domainKnowledgeScore", 95 + random.nextInt(5));
                        result.put("strengths", List.of("Correctly solved the MCQ.", "Clear understanding of the specific topic: " + (!expectedTopics.isEmpty() ? expectedTopics.get(0) : domainText) + "."));
                        result.put("weaknesses", new ArrayList<>());
                        result.put("missingConcepts", new ArrayList<>());
                        result.put("recommendations", List.of("Continue practicing more advanced topics.", "Maintain accuracy under time limits."));
                    } else {
                        result.put("technicalScore", 10 + random.nextInt(15));
                        result.put("communicationScore", 80 + random.nextInt(10));
                        result.put("completenessScore", 0);
                        result.put("confidenceScore", 90);
                        result.put("problemSolvingScore", 20 + random.nextInt(10));
                        result.put("domainKnowledgeScore", 10 + random.nextInt(15));
                        result.put("strengths", List.of("Attempted the question."));
                        result.put("weaknesses", List.of("Selected the incorrect option.", "Lacks depth in " + (!expectedTopics.isEmpty() ? expectedTopics.get(0) : domainText) + "."));
                        result.put("missingConcepts", !expectedTopics.isEmpty() ? expectedTopics : List.of("Core concepts"));
                        result.put("recommendations", List.of("Review the theoretical foundations of " + (!expectedTopics.isEmpty() ? expectedTopics.get(0) : domainText) + ".", "Practice step-by-step problem solving."));
                    }
                    return objectMapper.writeValueAsString(result);
                }

                // Evaluate text/code answers dynamically
                final String finalCandidateAnswer = candidateAnswer;
                long matchedTopicsCount = expectedTopics.stream()
                        .filter(topic -> finalCandidateAnswer.toLowerCase().contains(topic.toLowerCase()))
                        .count();

                int baseScore = 55;
                if (candidateAnswer.length() > 100) {
                    baseScore = 82;
                } else if (candidateAnswer.length() > 30) {
                    baseScore = 70;
                }

                int topicBonus = (int) (matchedTopicsCount * 8);
                int tech = Math.min(98, Math.max(30, baseScore + topicBonus));
                int comm = Math.min(98, Math.max(30, baseScore + (candidateAnswer.length() > 50 ? 5 : -5)));
                int comp = Math.min(100, Math.max(30, (int) Math.round(((double) matchedTopicsCount / Math.max(1, expectedTopics.size())) * 100)));
                int conf = Math.min(98, Math.max(30, baseScore + (candidateAnswer.length() > 70 ? 7 : -3)));
                int ps = Math.min(98, Math.max(30, baseScore + (candidateAnswer.contains("because") || candidateAnswer.contains("since") ? 10 : 0)));
                int dom = Math.min(98, Math.max(30, tech - 2));

                result.put("technicalScore", tech);
                result.put("communicationScore", comm);
                result.put("completenessScore", comp);
                result.put("confidenceScore", conf);
                result.put("problemSolvingScore", ps);
                result.put("domainKnowledgeScore", dom);

                if (tech >= 75) {
                    result.put("strengths", List.of("Good explanation of the topic.", "Demonstrated core domain knowledge in " + domainText + ".", "Appropriate use of technical terminology."));
                    result.put("weaknesses", List.of("Could provide more concrete examples or code snippets.", "Slight optimization points could be discussed."));
                    result.put("missingConcepts", new ArrayList<>());
                    result.put("recommendations", List.of("Study advanced patterns in " + domainText + ".", "Work on structuring answers using the STAR method."));
                } else {
                    result.put("strengths", List.of("Basic familiarity with the concepts.", "Coherent explanation structure."));
                    result.put("weaknesses", List.of("Answer lacks technical depth and detail.", "Failed to cover all expected topics like " + (expectedTopics.size() >= 2 ? expectedTopics.get(0) + ", " + expectedTopics.get(1) : domainText) + "."));
                    
                    List<String> missing = new ArrayList<>();
                    for (String t : expectedTopics) {
                        if (!candidateAnswer.toLowerCase().contains(t.toLowerCase())) {
                            missing.add(t);
                        }
                    }
                    result.put("missingConcepts", missing);
                    result.put("recommendations", List.of("Review fundamental tutorials on " + domainText + ".", "Practice explaining technical details in mock sessions."));
                }
                return objectMapper.writeValueAsString(result);

            } else if (prompt.contains("learningRoadmap")) {
                List<String> weaknesses = new ArrayList<>();
                List<String> missing = new ArrayList<>();

                Pattern weakPat = Pattern.compile("Weaknesses Observed:\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
                Matcher weakMat = weakPat.matcher(prompt);
                if (weakMat.find()) {
                    for (String w : weakMat.group(1).split(",")) {
                        String trim = w.trim();
                        if (!trim.isEmpty() && !trim.equalsIgnoreCase("None")) {
                            weaknesses.add(trim);
                        }
                    }
                }

                Pattern missPat = Pattern.compile("Missing Concepts:\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
                Matcher missMat = missPat.matcher(prompt);
                if (missMat.find()) {
                    for (String m : missMat.group(1).split(",")) {
                        String trim = m.trim();
                        if (!trim.isEmpty() && !trim.equalsIgnoreCase("None")) {
                            missing.add(trim);
                        }
                    }
                }

                List<String> roadmap = new ArrayList<>();
                if (!weaknesses.isEmpty() && !weaknesses.get(0).contains("Timed out") && !weaknesses.get(0).contains("No answer")) {
                    weaknesses.stream().limit(3).forEach(w -> {
                        roadmap.add("Improve on weakness: \"" + w + "\" by reading documentation and practicing related exercises.");
                    });
                }
                if (!missing.isEmpty()) {
                    missing.stream().limit(3).forEach(m -> {
                        roadmap.add("Study and master the missing concept: \"" + m + "\".");
                    });
                }

                roadmap.add("Schedule a follow-up mock interview to test your improvements.");
                roadmap.add("Spend 15-30 minutes daily practicing coding/problem solving on the platform.");

                List<String> limitedRoadmap = roadmap.stream().limit(5).collect(java.util.stream.Collectors.toList());
                return objectMapper.writeValueAsString(Map.of("learningRoadmap", limitedRoadmap));

            } else if (prompt.contains("extractedSkills") && prompt.contains("atsScore")) {
                String resumeText = "";
                int startIndex = prompt.indexOf("Resume Text:\n\"\"\"");
                if (startIndex != -1) {
                    int start = startIndex + "Resume Text:\n\"\"\"".length();
                    int end = prompt.indexOf("\"\"\"", start);
                    if (end != -1) {
                        resumeText = prompt.substring(start, end).trim();
                    }
                }
                if (resumeText.isEmpty()) {
                    resumeText = prompt;
                }

                List<String> extractedSkills = new ArrayList<>();
                List<Map<String, Object>> extractedProjects = new ArrayList<>();
                List<String> extractedExperience = new ArrayList<>();
                List<String> extractedEducation = new ArrayList<>();
                List<String> extractedCertifications = new ArrayList<>();

                String[] skillKeywords = {
                    "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Golang", "Go ", "Rust", "Ruby", "PHP", 
                    "HTML", "CSS", "SQL", "MongoDB", "PostgreSQL", "MySQL", "Redis", "Elasticsearch", "React", "Angular", 
                    "Vue", "Node.js", "Express", "Django", "Flask", "Spring Boot", "Spring", "Hibernate", "Docker", "Kubernetes", 
                    "AWS", "Azure", "GCP", "Git", "GitHub", "CI/CD", "Jenkins", "Terraform", "Ansible", "JUnit", "Selenium", 
                    "PyTorch", "TensorFlow", "Scikit-Learn", "Pandas", "NumPy", "Spark", "Hadoop", "Tableau", "PowerBI", 
                    "Machine Learning", "Data Science", "Data Analysis", "REST API", "GraphQL", "Microservices"
                };

                for (String skill : skillKeywords) {
                    Pattern p = Pattern.compile("\\b" + Pattern.quote(skill.trim()) + "\\b", Pattern.CASE_INSENSITIVE);
                    if (p.matcher(resumeText).find()) {
                        extractedSkills.add(skill.trim());
                    }
                }
                if (extractedSkills.isEmpty()) {
                    extractedSkills.addAll(List.of("Software Engineering", "Problem Solving", "Programming"));
                }

                String[] lines = resumeText.split("\\r?\\n");
                String currentSection = "";
                List<String> potentialProjectTitles = new ArrayList<>();

                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;
                    
                    String lower = trimmed.toLowerCase();
                    if (lower.matches("^(projects|personal projects|academic projects|technical projects)[:\\s]*$") || 
                        (lower.contains("project") && trimmed.length() < 25 && !lower.contains("using") && !lower.contains("developed"))) {
                        currentSection = "projects";
                        continue;
                    } else if (lower.matches("^(experience|work experience|employment history|professional experience|experience history)[:\\s]*$")) {
                        currentSection = "experience";
                        continue;
                    } else if (lower.matches("^(education|academic background|academics)[:\\s]*$")) {
                        currentSection = "education";
                        continue;
                    } else if (lower.matches("^(certifications|licenses|certifications & licenses|certificates)[:\\s]*$")) {
                        currentSection = "certifications";
                        continue;
                    } else if (trimmed.length() < 30 && (lower.startsWith("skills") || lower.startsWith("technical skills"))) {
                        currentSection = "skills";
                        continue;
                    }

                    if ("education".equals(currentSection) || lower.contains("university") || lower.contains("college") || lower.contains("bachelor") || lower.contains("master") || lower.contains("degree")) {
                        if (extractedEducation.size() < 3 && !extractedEducation.contains(trimmed) && trimmed.length() > 5 && trimmed.length() < 100) {
                            extractedEducation.add(trimmed);
                        }
                    } else if ("certifications".equals(currentSection) || lower.contains("certified") || lower.contains("certification") || lower.contains("certificate")) {
                        if (extractedCertifications.size() < 4 && !extractedCertifications.contains(trimmed) && trimmed.length() > 5 && trimmed.length() < 100) {
                            extractedCertifications.add(trimmed);
                        }
                    } else if ("experience".equals(currentSection)) {
                        if (extractedExperience.size() < 3 && trimmed.length() > 10 && trimmed.length() < 120 && !trimmed.startsWith("-") && !trimmed.startsWith("*") && !trimmed.startsWith("•")) {
                            extractedExperience.add(trimmed);
                        }
                    } else if ("projects".equals(currentSection)) {
                        if (potentialProjectTitles.size() < 5 && trimmed.length() > 3 && trimmed.length() < 50 && !trimmed.startsWith("-") && !trimmed.startsWith("*") && !trimmed.startsWith("•")) {
                            potentialProjectTitles.add(trimmed);
                        }
                    }
                }

                // Fallbacks
                if (extractedEducation.isEmpty()) {
                    for (String line : lines) {
                        String trimmed = line.trim();
                        String lower = trimmed.toLowerCase();
                        if (lower.contains("university") || lower.contains("college") || lower.contains("bachelor") || lower.contains("master") || lower.contains("degree") || lower.contains("b.tech") || lower.contains("b.e.") || lower.contains("b.s.")) {
                            if (extractedEducation.size() < 2 && trimmed.length() > 5 && trimmed.length() < 100) {
                                extractedEducation.add(trimmed);
                            }
                        }
                    }
                }
                if (extractedEducation.isEmpty()) {
                    extractedEducation.add("B.S. in Computer Science or related engineering field");
                }

                if (extractedCertifications.isEmpty()) {
                    for (String line : lines) {
                        String trimmed = line.trim();
                        String lower = trimmed.toLowerCase();
                        if (lower.contains("certified") || lower.contains("certification") || lower.contains("credential") || lower.contains("certificate")) {
                            if (extractedCertifications.size() < 3 && trimmed.length() > 5 && trimmed.length() < 100) {
                                extractedCertifications.add(trimmed);
                            }
                        }
                    }
                }
                if (extractedCertifications.isEmpty()) {
                    extractedCertifications.add("Technical Certifications (Inferred from coursework)");
                }

                if (extractedExperience.isEmpty()) {
                    for (String line : lines) {
                        String trimmed = line.trim();
                        String lower = trimmed.toLowerCase();
                        if (lower.contains("intern") || lower.contains("engineer") || lower.contains("developer") || lower.contains("analyst")) {
                            if (extractedExperience.size() < 3 && trimmed.length() > 10 && trimmed.length() < 100 && !trimmed.startsWith("-") && !trimmed.startsWith("*")) {
                                extractedExperience.add(trimmed);
                            }
                        }
                    }
                }
                if (extractedExperience.isEmpty()) {
                    extractedExperience.add("Software Engineer Intern");
                }

                for (String title : potentialProjectTitles) {
                    List<String> projectTechs = new ArrayList<>();
                    for (String skill : extractedSkills) {
                        if (resumeText.toLowerCase().contains(skill.toLowerCase())) {
                            if (projectTechs.size() < 4 && Math.random() > 0.3) {
                                projectTechs.add(skill);
                            }
                        }
                    }
                    if (projectTechs.isEmpty()) {
                        projectTechs.add(extractedSkills.get(0));
                    }
                    Map<String, Object> proj = new HashMap<>();
                    proj.put("name", title);
                    proj.put("technologies", projectTechs);
                    proj.put("complexity", projectTechs.size() > 2 ? "Advanced" : "Intermediate");
                    proj.put("readinessScore", (double) (75 + new Random().nextInt(20)));
                    extractedProjects.add(proj);
                }

                if (extractedProjects.isEmpty()) {
                    if (extractedSkills.contains("React") || extractedSkills.contains("JavaScript") || extractedSkills.contains("TypeScript")) {
                        List<String> reactTechs = new ArrayList<>();
                        if (extractedSkills.contains("React")) reactTechs.add("React");
                        if (extractedSkills.contains("JavaScript")) reactTechs.add("JavaScript");
                        if (extractedSkills.contains("Node.js")) reactTechs.add("Node.js");
                        if (extractedSkills.contains("MongoDB")) reactTechs.add("MongoDB");
                        if (reactTechs.isEmpty()) reactTechs.add(extractedSkills.get(0));
                        
                        Map<String, Object> proj1 = new HashMap<>();
                        proj1.put("name", "Full-Stack Web Portal");
                        proj1.put("technologies", reactTechs);
                        proj1.put("complexity", "Intermediate");
                        proj1.put("readinessScore", 80.0);
                        extractedProjects.add(proj1);
                    }
                    if (extractedSkills.contains("Java") || extractedSkills.contains("Spring Boot") || extractedSkills.contains("Python")) {
                        List<String> backendTechs = new ArrayList<>();
                        if (extractedSkills.contains("Java")) backendTechs.add("Java");
                        if (extractedSkills.contains("Spring Boot")) backendTechs.add("Spring Boot");
                        if (extractedSkills.contains("SQL")) backendTechs.add("SQL");
                        if (extractedSkills.contains("Docker")) backendTechs.add("Docker");
                        if (backendTechs.isEmpty()) backendTechs.add(extractedSkills.get(0));
                        
                        Map<String, Object> proj2 = new HashMap<>();
                        proj2.put("name", "Scalable Backend Service");
                        proj2.put("technologies", backendTechs);
                        proj2.put("complexity", "Advanced");
                        proj2.put("readinessScore", 85.0);
                        extractedProjects.add(proj2);
                    }
                    if (extractedProjects.isEmpty()) {
                        Map<String, Object> projFallback = new HashMap<>();
                        projFallback.put("name", "Systems Engineering Project");
                        projFallback.put("technologies", List.of(extractedSkills.get(0)));
                        projFallback.put("complexity", "Intermediate");
                        projFallback.put("readinessScore", 78.0);
                        extractedProjects.add(projFallback);
                    }
                }

                double technicalSkillScore = Math.round(55.0 + Math.min(43.0, extractedSkills.size() * 4.0));
                double projectQualityScore = Math.round(60.0 + Math.min(38.0, extractedProjects.size() * 10.0));
                double experienceScore = Math.round(50.0 + Math.min(48.0, extractedExperience.size() * 15.0));
                double keywordMatchScore = Math.round(55.0 + Math.min(43.0, extractedSkills.size() * 3.5));
                
                double formattingScore = 70.0;
                if (resumeText.toLowerCase().contains("education")) formattingScore += 10;
                if (resumeText.toLowerCase().contains("experience")) formattingScore += 10;
                if (resumeText.toLowerCase().contains("projects")) formattingScore += 10;
                formattingScore = Math.min(98.0, formattingScore);
                
                double resumeScore = Math.round((technicalSkillScore + projectQualityScore + experienceScore + keywordMatchScore + formattingScore) / 5.0);
                double atsScore = Math.round(keywordMatchScore * 0.35 + experienceScore * 0.25 + technicalSkillScore * 0.20 + formattingScore * 0.20);
                double industryReadinessScore = Math.round((projectQualityScore + experienceScore + technicalSkillScore) / 3.0);

                List<String> strengths = new ArrayList<>();
                strengths.add("Extracted core technical skills: " + String.join(", ", extractedSkills.subList(0, Math.min(4, extractedSkills.size()))));
                strengths.add("Demonstrated practical application in project: \"" + extractedProjects.get(0).get("name") + "\"");
                strengths.add("Clear grounding in " + extractedEducation.get(0));

                List<String> weaknesses = new ArrayList<>();
                if (extractedSkills.size() < 6) {
                    weaknesses.add("Limited technology breadth. Tech stack could be expanded.");
                }
                if (extractedExperience.size() < 2) {
                    weaknesses.add("Lacks extensive professional industry experience details.");
                }
                weaknesses.add("Resume descriptions could benefit from more quantifiable, data-driven achievements.");

                List<String> recommendations = new ArrayList<>();
                recommendations.add("Quantify your project achievements (e.g., 'reduced API response times by 30%').");
                if (extractedSkills.size() < 8) {
                    recommendations.add("Acquire skills in highly demanded ecosystem technologies (e.g., Docker, cloud platforms).");
                }
                recommendations.add("Refine the experience section descriptions using action-oriented verbs.");

                List<String> standardTechs = List.of("Docker", "CI/CD", "Kubernetes", "AWS", "TypeScript", "Redis", "Elasticsearch", "Unit Testing");
                List<String> missingSkills = new ArrayList<>();
                List<String> missingTechnologies = new ArrayList<>();
                for (String tech : standardTechs) {
                    if (!extractedSkills.contains(tech)) {
                        if (missingSkills.size() < 2) {
                            missingSkills.add(tech);
                        } else if (missingTechnologies.size() < 2) {
                            missingTechnologies.add(tech);
                        }
                    }
                }
                if (missingSkills.isEmpty()) missingSkills.add("Cloud Deployments");
                if (missingTechnologies.isEmpty()) missingTechnologies.add("System Orchestration");

                String mainSkills = extractedSkills.size() > 3 ? String.join(", ", extractedSkills.subList(0, 3)) : String.join(", ", extractedSkills);
                String resumeSummary = "Professional candidate with strong capabilities in " + mainSkills + ". " +
                                       "Proven experience with projects like \"" + extractedProjects.get(0).get("name") + "\" " +
                                       "and academic or industry background: " + extractedEducation.get(0) + ".";

                Map<String, Object> resumeData = new HashMap<>();
                resumeData.put("extractedSkills", extractedSkills);
                resumeData.put("extractedProjects", extractedProjects);
                resumeData.put("extractedExperience", extractedExperience);
                resumeData.put("extractedEducation", extractedEducation);
                resumeData.put("extractedCertifications", extractedCertifications);
                resumeData.put("atsScore", atsScore);
                resumeData.put("resumeScore", resumeScore);
                resumeData.put("industryReadinessScore", industryReadinessScore);
                resumeData.put("keywordMatchScore", keywordMatchScore);
                resumeData.put("projectQualityScore", projectQualityScore);
                resumeData.put("experienceScore", experienceScore);
                resumeData.put("technicalSkillScore", technicalSkillScore);
                resumeData.put("formattingScore", formattingScore);
                resumeData.put("strengths", strengths);
                resumeData.put("weaknesses", weaknesses);
                resumeData.put("recommendations", recommendations);
                resumeData.put("missingSkills", missingSkills);
                resumeData.put("missingTechnologies", missingTechnologies);
                resumeData.put("resumeSummary", resumeSummary);
                return objectMapper.writeValueAsString(resumeData);

            } else if (prompt.contains("ORIGINAL_QUESTION") || (prompt.contains("\"question\": \"string\"") && !prompt.contains("["))) {
                Map<String, Object> question = new HashMap<>();
                question.put("question", "Can you elaborate on how you handled state management in that scenario?");
                question.put("category", "Technical Theory");
                question.put("domain", "Frontend");
                question.put("difficulty", "Medium");
                question.put("expectedTopics", List.of("Redux", "Context API"));
                question.put("evaluationCriteria", List.of("Clarity", "Depth of knowledge"));
                question.put("hints", List.of("Think about global vs local state"));
                question.put("qualityScore", 90);
                question.put("tags", List.of("React", "State"));
                return objectMapper.writeValueAsString(question);

            } else {
                // Default Case: Question list generation
                int count = 3;
                Pattern countPat = Pattern.compile("generate (\\d+) ", Pattern.CASE_INSENSITIVE);
                Matcher countMat = countPat.matcher(prompt);
                if (countMat.find()) {
                    count = Integer.parseInt(countMat.group(1));
                }

                String domain = (String) options.getOrDefault("domain", "");
                if (domain.isEmpty()) {
                    Pattern domPat = Pattern.compile("domain '([^']+)'", Pattern.CASE_INSENSITIVE);
                    Matcher domMat = domPat.matcher(prompt);
                    if (domMat.find()) {
                        domain = domMat.group(1).trim();
                    } else {
                        Pattern domPat2 = Pattern.compile("domain:\\s*([^\n]+)", Pattern.CASE_INSENSITIVE);
                        Matcher domMat2 = domPat2.matcher(prompt);
                        if (domMat2.find()) {
                            domain = domMat2.group(1).trim();
                        }
                    }
                }

                String interviewType = (String) options.getOrDefault("interviewType", "");
                boolean isHR = "HR".equals(interviewType) || prompt.contains("HR/Behavioral Interviewer");
                boolean isResumeInterview = prompt.contains("Skills & Tech Stack:");
                boolean isDSA = !isHR && "DSA".equals(domain);
                boolean isReact = !isHR && "React".equals(domain);
                boolean isNode = !isHR && "Node.js".equals(domain);
                boolean isJava = !isHR && "Java".equals(domain);
                boolean isBackend = !isHR && "Backend".equals(domain);
                boolean isFullStack = !isHR && "Full Stack".equals(domain);
                boolean isAptitude = !isHR && (
                        "Aptitude".equals(interviewType) ||
                        prompt.contains("Aptitude test questions") ||
                        prompt.contains("Aptitude/Reasoning") ||
                        "Aptitude".equals(domain) ||
                        domain.contains("Aptitude") ||
                        domain.contains("Reasoning") ||
                        List.of("Mixed Aptitude", "Quantitative Aptitude", "Logical Reasoning", "Verbal Ability", "Data Interpretation", "Puzzles").contains(domain)
                );

                if (!isHR && !isAptitude && !isResumeInterview) {
                    isAptitude = prompt.contains("Aptitude test") || prompt.contains("Quantitative") || prompt.contains("Logical Reasoning");
                }

                List<Map<String, Object>> questions = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    if (isHR) {
                        Map<String, Object> q = getMockHrQuestion(domain, i);
                        questions.add(q);
                    } else if (isAptitude) {
                        Map<String, Object> q = getMockAptitudeQuestion(domain, i);
                        questions.add(q);
                    } else if (isResumeInterview) {
                        Map<String, Object> q = getMockResumeQuestion(prompt, i);
                        questions.add(q);
                    } else if (isDSA) {
                        Map<String, Object> q = getMockDsaQuestion(i);
                        questions.add(q);
                    } else if (isReact) {
                        Map<String, Object> q = getMockReactQuestion(i);
                        questions.add(q);
                    } else if (isNode) {
                        Map<String, Object> q = getMockNodeQuestion(i);
                        questions.add(q);
                    } else if (isJava) {
                        Map<String, Object> q = getMockJavaQuestion(i);
                        questions.add(q);
                    } else if (isBackend) {
                        Map<String, Object> q = getMockBackendQuestion(i);
                        questions.add(q);
                    } else if (isFullStack) {
                        Map<String, Object> q = getMockFullStackQuestion(i);
                        questions.add(q);
                    } else {
                        Map<String, Object> q = getMockGeneralQuestion(domain, i);
                        questions.add(q);
                    }
                }
                
                return objectMapper.writeValueAsString(Map.of("questions", questions));
            }
        } catch (Exception e) {
            log.error("Failed to generate mock OpenAI response", e);
            return "{}";
        }
    }

    private boolean isPoorAnswer(String answer) {
        String cleaned = answer.replaceAll("[\".?]", "").trim().toLowerCase();
        return cleaned.matches("^(skip|don't know|no idea|pass|no answer|unknown|dunno|none|na|n/a|no|i don't know)$");
    }

    private Map<String, String> getMockMcqAnswers() {
        Map<String, String> answers = new HashMap<>();
        answers.put("increased by 20% and then decreased by 20%", "4% decrease");
        answers.put("gets 84% of the votes", "700");
        answers.put("sells an article for $400", "$320");
        answers.put("selling a book for $115.20", "$134.40");
        answers.put("do a piece of work in 10 days", "6 days");
        answers.put("twice as good a workman as B", "27 days");
        answers.put("train 120m long passes a telegraph post", "72 km/h");
        answers.put("travels at 30 km/h", "30 km");
        answers.put("probability of getting a sum of 7", "1/6");
        answers.put("contains 6 black and 8 white balls", "4/7");
        answers.put("letters of the word LEADING", "720");
        answers.put("words of 3 consonants and 2 vowels", "25200");
        answers.put("A:B = 2:3 and B:C = 4:5", "8:12:15");
        answers.put("ratio of the ages of two persons is 5:7", "50, 70");
        answers.put("average of 5 consecutive odd numbers is 61", "65");
        answers.put("average weight of 8 persons increases by 2.5 kg", "85 kg");
        answers.put("Which of the following is a prime number", "127");
        answers.put("sum of the first 25 natural numbers", "325");
        answers.put("series: 2, 1, (1/2), (1/4)", "1/8");
        answers.put("word does NOT belong with the others", "mayonnaise");
        answers.put("Pointing to a photograph, a man said", "His son's");
        answers.put("A is the brother of B; B is the sister of C", "Uncle");
        answers.put("COMPUTER\" is written as \"RFUVQNPC", "EOJDJEFM");
        answers.put("POPULAR is coded as QPQVMBS", "FAMOUS");
        answers.put("Three friends A, B, and C are sitting in a row", "A");
        answers.put("sitting on a bench. A is next to B", "Between B and C");
        answers.put("budget of $5000 where Rent is 30%", "$250");
        answers.put("grades, A grade is 90 degrees", "25%");
        answers.put("synonym for \"Benevolent\"", "Kind");
        answers.put("He is one of the best player", "best player");
        answers.put("purpose of useEffect", "Side Effects");
        answers.put("re-renders unnecessarily. Which React feature", "useMemo");
        answers.put("used to access a DOM element directly", "useRef");
        answers.put("hook is used for side effects in React", "useEffect");
        answers.put("module is used to create an HTTP server", "http");
        answers.put("module in Node.js is used to create an HTTP server", "http");
        answers.put("what is middleware in Express", "Request Processing Function");
        answers.put("describes the Node.js event loop", "Single-threaded Non-blocking");
        answers.put("collection allows duplicate elements", "List");
        answers.put("purpose of the Streams API", "Functional-style operations on collections");
        answers.put("prevents a method from being overridden", "final");
        answers.put("purpose of JWT", "Authentication");
        answers.put("HTTP method is idempotent and used to replace", "PUT");
        answers.put("what does a Reverse Proxy do", "Distributes client requests to backend servers");
        answers.put("OS scheduling algorithm", "Round Robin");
        answers.put("NOT an ACID property", "Distribution");
        answers.put("advantage of decoupling the frontend from the backend", "Independent deployment and scaling");
        answers.put("scalable architecture for your resume projects", "Database connections/locks");
        return answers;
    }

    private Map<String, Object> getMockHrQuestion(String domain, int i) {
        String normalizedDomain = domain == null ? "" : domain.trim().toLowerCase();
        
        String[] questions;
        List<String> criteria = new ArrayList<>(List.of("Communication", "Clarity", "Relevance", "Use of STAR Method"));
        
        if (normalizedDomain.contains("software engineering") || normalizedDomain.contains("software engineer")) {
            questions = new String[]{
                "Tell me about a production issue you resolved.",
                "Describe a time you had conflicting implementation approaches with another engineer.",
                "How did you handle changing requirements during a sprint?",
                "Can you discuss a time when you had to make a trade-off between clean code and fast delivery?",
                "Tell me about a time you had to onboard onto a large, unfamiliar codebase quickly."
            };
            criteria.add("Engineering Best Practices");
        } else if (normalizedDomain.contains("full stack") || normalizedDomain.contains("fullstack")) {
            questions = new String[]{
                "Tell me about a time you had to deliver a feature end-to-end from DB schema to frontend UI under a tight deadline.",
                "How do you handle disputes or design choices regarding API contracts between client and server developers?",
                "Describe a challenging issue you debugged where the root cause was in a database, but manifested in the UI.",
                "How do you balance learning both frontend and backend technologies while maintaining deep expertise?",
                "Describe a time you optimized a full-stack feature's performance."
            };
            criteria.add("Full Stack Integration");
        } else if (normalizedDomain.contains("frontend") || normalizedDomain.contains("front end")) {
            questions = new String[]{
                "Describe a time you had to implement a design that had technical limitations. How did you negotiate with the designer?",
                "How did you handle a situation where a frontend page or feature had performance issues on mobile devices?",
                "Talk about a time you had to quickly learn a new CSS framework or frontend build tool for a project.",
                "How do you ensure web accessibility (a11y) and SEO friendliness are integrated into your styling workflow?",
                "Tell me about a time when you received negative feedback on a UI component you built, and how you iterated on it."
            };
            criteria.add("UX Empathy");
        } else if (normalizedDomain.contains("backend") || normalizedDomain.contains("back end")) {
            questions = new String[]{
                "Describe a time you optimized a slow API.",
                "How did you design a scalable backend solution?",
                "Tell me about handling database performance issues.",
                "Describe a scenario where you had to integrate a complex third-party API and handle failure states.",
                "Tell me about a time you had to migrate data or schema in a production database without downtime."
            };
            criteria.add("System Scaling & Reliability");
        } else if (normalizedDomain.contains("java")) {
            questions = new String[]{
                "Tell me about a performance bottleneck you solved in a Java application.",
                "Describe a challenging Spring Boot issue you debugged.",
                "How have you improved code quality in a Java project?",
                "Tell me about a time you had to handle complex multithreading or concurrency issues in Java.",
                "Describe how you designed a database integration layer using Hibernate/JPA and handled transaction boundaries."
            };
            criteria.add("Java Ecosystem Best Practices");
        } else if (normalizedDomain.contains("react")) {
            questions = new String[]{
                "Describe a difficult state management problem you solved.",
                "Tell me about improving frontend performance.",
                "How did you handle complex component communication?",
                "Describe a time when you had to design a highly reusable component library or UI system.",
                "Tell me about a time you had to debug a memory leak or layout lag in a React application."
            };
            criteria.add("React Best Practices");
        } else if (normalizedDomain.contains("node")) {
            questions = new String[]{
                "Describe a time when a blocking event-loop operation caused latency in your Node.js application. How did you resolve it?",
                "Tell me about your approach to structuring middleware for a complex authentication flow.",
                "How did you handle a memory leak in a Node.js production service?",
                "Describe a scenario where you set up event-driven communication between Node.js microservices.",
                "Tell me about a time you optimized database query execution or caching in an Express/NestJS backend."
            };
            criteria.add("Node.js Performance & Reliability");
        } else if (normalizedDomain.contains("data analyst")) {
            questions = new String[]{
                "Describe a time when you had to clean a messy and unstructured dataset under a tight deadline.",
                "How did you handle a situation where a stakeholder disagreed with the insights or data report you presented?",
                "Tell me about a time you identified a business opportunity or solved a problem purely through data analysis.",
                "Describe a time you had to build a dashboard that needed to be refreshed in real-time. How did you structure the pipeline?",
                "How do you ensure data accuracy and validate your SQL queries before delivering reports."
            };
            criteria.add("Analytical Integrity & Business Impact");
        } else if (normalizedDomain.contains("data scientist") || normalizedDomain.contains("data science")) {
            questions = new String[]{
                "Describe a time when your ML model performed well in training but degraded in production. How did you resolve it?",
                "Tell me about a time you had to explain a complex model's decisions to a non-technical executive.",
                "How did you handle a project where the data was heavily imbalanced or lacking labeled samples?",
                "Describe how you decided between using a simple linear model versus a complex deep learning model for a specific problem.",
                "Tell me about a time you did feature engineering that resulted in a significant boost in model metrics."
            };
            criteria.add("Statistical Rigor & Model Deployment");
        } else if (normalizedDomain.contains("devops")) {
            questions = new String[]{
                "Describe a time you resolved a major production infrastructure outage. How did you coordinate communication?",
                "How did you handle a situation where developers were bypassing CI/CD security or quality checks?",
                "Tell me about a time you migrated a legacy deployment to a containerized infrastructure under tight constraints.",
                "Describe how you managed infrastructure-as-code configuration drifts across environments.",
                "Tell me about an automation script or tool you built that saved hours of manual deployment work."
            };
            criteria.add("Infrastructure Automation & Outage Recovery");
        } else if (normalizedDomain.contains("qa") || normalizedDomain.contains("quality assurance") || normalizedDomain.contains("testing")) {
            questions = new String[]{
                "Describe a situation where you found a blocker bug right before a release. How did you handle the communication with stakeholders?",
                "Talk about a time a developer insisted a bug you filed was 'not a bug', but you felt it was critical. How did you resolve this?",
                "Tell me about a time you automated a complex test suite that significantly reduced regression testing time.",
                "How do you design a test plan for a feature that has very vague or incomplete requirements?",
                "Describe a time when a critical bug slipped into production. How did you update your testing strategy?"
            };
            criteria.add("Quality Advocacy & Test Automation");
        } else if (normalizedDomain.contains("product manager") || normalizedDomain.equals("pm")) {
            questions = new String[]{
                "Tell me about a time you had to say 'no' to an important stakeholder's feature request. How did you manage the relationship?",
                "Describe a product launch that did not meet its target metrics. What did you learn and how did you pivot?",
                "How do you resolve conflicts between engineering estimates and business deadline expectations?",
                "Tell me about a time when user feedback contradicted your initial product roadmap. How did you adjust?",
                "Describe how you defined and tracked key performance indicators (KPIs) for a newly launched feature."
            };
            criteria.add("Stakeholder Alignment & Product Strategy");
        } else {
            questions = new String[]{
                "Tell me about yourself. Walk me through your background and what brings you to this role.",
                "What is your greatest professional strength, and how have you applied it in a work situation?",
                "Describe your biggest professional weakness and the steps you have taken to address it.",
                "Tell me about a time you had a conflict with a colleague. How did you resolve it?",
                "Where do you see yourself in five years, and how does this role align with your goals?"
            };
        }

        String qText = questions[i % questions.length];
        
        Map<String, Object> q = new HashMap<>();
        q.put("question", qText);
        q.put("category", "Behavioral");
        q.put("domain", domain == null || domain.isEmpty() ? "General HR" : domain);
        q.put("difficulty", "Medium");
        q.put("expectedTopics", List.of("Self-awareness", "Communication", "Problem Solving"));
        q.put("evaluationCriteria", criteria);
        q.put("hints", List.of("Use the STAR method: Situation, Task, Action, Result", "Be specific and give a real example"));
        q.put("qualityScore", 92);
        q.put("tags", List.of("HR", "Behavioral"));
        q.put("questionFormat", "text");
        return q;
    }

    private Map<String, Object> getMockAptitudeQuestion(String domain, int i) {
        Map<String, Object> q = new HashMap<>();
        q.put("category", "Aptitude");
        q.put("domain", domain.isEmpty() ? "Aptitude" : domain);
        q.put("difficulty", "Medium");
        q.put("evaluationCriteria", List.of("Accuracy", "Speed"));
        q.put("hints", List.of("Read the problem statement carefully", "Use paper to calculate"));
        q.put("qualityScore", 90);
        q.put("questionFormat", "mcq");

        if (i % 2 == 0) {
            q.put("question", "If the price of a book is increased by 20% and then decreased by 20%, what is the net change?");
            q.put("options", List.of("No change", "4% increase", "4% decrease", "10% decrease"));
            q.put("correctOption", "4% decrease");
            q.put("expectedTopics", List.of("Percentages"));
            q.put("tags", List.of("Aptitude", "MCQ", "Percentages"));
        } else {
            q.put("question", "If A can do a piece of work in 10 days and B can do it in 15 days, how long will they take working together?");
            q.put("options", List.of("5 days", "6 days", "8 days", "12 days"));
            q.put("correctOption", "6 days");
            q.put("expectedTopics", List.of("Time and Work"));
            q.put("tags", List.of("Aptitude", "MCQ", "Time and Work"));
        }
        return q;
    }

    private Map<String, Object> getMockResumeQuestion(String prompt, int i) {
        List<String> skills = new ArrayList<>();
        List<String> projects = new ArrayList<>();
        List<String> experiences = new ArrayList<>();
        List<String> certifications = new ArrayList<>();

        // Extract skills
        int skillsIdx = prompt.indexOf("Skills & Tech Stack:");
        if (skillsIdx != -1) {
            int end = prompt.indexOf("\n", skillsIdx);
            if (end != -1) {
                String line = prompt.substring(skillsIdx + "Skills & Tech Stack:".length(), end).trim();
                for (String s : line.split(",")) {
                    String trim = s.trim();
                    if (!trim.isEmpty() && !trim.equalsIgnoreCase("None")) skills.add(trim);
                }
            }
        }
        // Extract projects
        int projectsIdx = prompt.indexOf("Projects:");
        if (projectsIdx != -1) {
            int end = prompt.indexOf("\n", projectsIdx);
            if (end != -1) {
                String line = prompt.substring(projectsIdx + "Projects:".length(), end).trim();
                String cleanedLine = line.replaceAll("\\([^)]*\\)", "");
                for (String p : cleanedLine.split(",")) {
                    String trim = p.trim();
                    if (!trim.isEmpty() && !trim.equalsIgnoreCase("None")) projects.add(trim);
                }
            }
        }
        // Extract experience
        int expIdx = prompt.indexOf("Experience:");
        if (expIdx != -1) {
            int end = prompt.indexOf("\n", expIdx);
            if (end != -1) {
                String line = prompt.substring(expIdx + "Experience:".length(), end).trim();
                for (String exp : line.split(",")) {
                    String trim = exp.trim();
                    if (!trim.isEmpty() && !trim.equalsIgnoreCase("None")) experiences.add(trim);
                }
            }
        }
        // Extract certifications
        int certIdx = prompt.indexOf("Certifications:");
        if (certIdx != -1) {
            int end = prompt.indexOf("\n", certIdx);
            if (end != -1) {
                String line = prompt.substring(certIdx + "Certifications:".length(), end).trim();
                for (String c : line.split(",")) {
                    String trim = c.trim();
                    if (!trim.isEmpty() && !trim.equalsIgnoreCase("None")) certifications.add(trim);
                }
            }
        }

        // Fallbacks if empty
        if (skills.isEmpty()) skills.add("Software Engineering");
        if (projects.isEmpty()) projects.add("InterviewIQ Portal");
        if (experiences.isEmpty()) experiences.add("Software Engineer Intern");
        if (certifications.isEmpty()) certifications.add("AWS Certified Developer");

        String qText;
        List<String> expectedTopics = new ArrayList<>(List.of("Architecture", "Implementation"));
        List<String> tags = new ArrayList<>(List.of("Resume", "Project"));

        // Question distribution:
        // 60-70% Resume-specific (projects, tech, architecture, challenges, experience)
        // 20-30% Domain-specific (prioritizing mentioned technologies)
        // 10-20% General behavioral (grounded in resume projects/experience)
        
        int pattern = i % 5;
        Random rand = new Random(i * 31L + projects.size() * 17L); // Seeded for slight variation but consistency
        
        if (pattern == 0 && !projects.isEmpty()) {
            // Project direct reference constraint
            String pName = projects.get(rand.nextInt(projects.size()));
            qText = "Explain your architecture and technical implementation details for the project: '" + pName + "'.";
            expectedTopics.add(pName);
            tags.add("Projects");
        } else if (pattern == 1 && !skills.isEmpty()) {
            // Technology prioritization constraint
            String skill = skills.get(rand.nextInt(skills.size()));
            qText = "Describe a time when you solved a challenging technical issue or built a feature using " + skill + ".";
            expectedTopics.add(skill);
            tags.add("Skills");
        } else if (pattern == 2 && !experiences.isEmpty()) {
            // Experience constraint
            String exp = experiences.get(rand.nextInt(experiences.size()));
            qText = "What were your primary responsibilities and the key engineering challenges you faced during your experience: '" + exp + "'?";
            expectedTopics.add("Responsibilities");
            tags.add("Experience");
        } else if (pattern == 3) {
            // Domain-specific question (prioritizing their technologies)
            if (skills.contains("Java") || skills.contains("Spring Boot")) {
                String[] javaQs = {
                    "Tell me about a performance bottleneck you solved in a Java application.",
                    "Describe a challenging Spring Boot issue you debugged.",
                    "How have you improved code quality in a Java project?"
                };
                qText = javaQs[rand.nextInt(javaQs.length)];
                expectedTopics.addAll(List.of("Java", "Performance"));
                tags.add("Java");
            } else if (skills.contains("React") || skills.contains("TypeScript")) {
                String[] reactQs = {
                    "Describe a difficult state management problem you solved in React.",
                    "Tell me about improving frontend performance in a React application.",
                    "How did you handle complex component communication in React?"
                };
                qText = reactQs[rand.nextInt(reactQs.length)];
                expectedTopics.addAll(List.of("React", "Frontend"));
                tags.add("React");
            } else {
                qText = "In the context of the technologies mentioned in your resume, how would you design a scalable backend solution or optimize a slow API?";
                expectedTopics.add("Scalability");
                tags.add("Backend");
            }
        } else {
            // General behavioral question grounded in resume projects/experience
            String pName = projects.get(rand.nextInt(projects.size()));
            String[] behaviorQs = {
                "How did you handle changing requirements or shifting deadlines during your work on '" + pName + "'?",
                "Describe a time you had conflicting implementation approaches with another engineer while building '" + pName + "'. How did you resolve it?",
                "Tell me about a major production issue or bug you encountered in '" + pName + "' and how you resolved it."
            };
            qText = behaviorQs[rand.nextInt(behaviorQs.length)];
            expectedTopics.add("Collaboration");
            tags.add("Behavioral");
        }

        Map<String, Object> q = new HashMap<>();
        q.put("question", qText);
        q.put("category", "Resume Discussion");
        q.put("domain", "Mixed");
        q.put("difficulty", "Medium");
        q.put("expectedTopics", expectedTopics);
        q.put("evaluationCriteria", List.of("Accuracy", "Clarity", "Use of STAR Method"));
        q.put("hints", List.of("Describe your own contribution and decision rationale."));
        q.put("qualityScore", 95);
        q.put("tags", tags);
        q.put("questionFormat", "text");
        return q;
    }

    private Map<String, Object> getMockDsaQuestion(int i) {
        Map<String, Object> q = new HashMap<>();
        q.put("question", "Solve the problem: Reverse Linked List");
        q.put("category", "DSA");
        q.put("domain", "DSA");
        q.put("difficulty", "Medium");
        q.put("expectedTopics", List.of("Linked Lists"));
        q.put("evaluationCriteria", List.of("Time Complexity", "Space Complexity", "Edge Cases"));
        q.put("hints", List.of("Consider iterative vs recursive approaches", "Use temporary pointers"));
        q.put("qualityScore", 95);
        q.put("tags", List.of("Coding", "Algorithms"));
        q.put("questionFormat", "code");
        q.put("starterCode", "function solve(head) {\n  // Write your code here\n  return head;\n}");
        
        Map<String, Object> tc = new HashMap<>();
        tc.put("input", "[1,2,3,4,5]");
        tc.put("expectedOutput", "[5,4,3,2,1]");
        q.put("testCases", List.of(tc));
        return q;
    }

    private Map<String, Object> getMockReactQuestion(int i) {
        Map<String, Object> q = new HashMap<>();
        q.put("category", "Technical Theory");
        q.put("domain", "React");
        q.put("difficulty", "Medium");
        q.put("evaluationCriteria", List.of("Accuracy"));
        q.put("qualityScore", 90);
        q.put("questionFormat", "mcq");
        q.put("tags", List.of("React"));

        if (i % 2 == 0) {
            q.put("question", "What is the purpose of useEffect?");
            q.put("options", List.of("State Management", "Side Effects", "Routing", "Styling"));
            q.put("correctOption", "Side Effects");
            q.put("expectedTopics", List.of("Hooks"));
        } else {
            q.put("question", "A component re-renders unnecessarily. Which React feature would help optimize performance?");
            q.put("options", List.of("useMemo", "useState", "useEffect", "Context API"));
            q.put("correctOption", "useMemo");
            q.put("expectedTopics", List.of("Performance"));
        }
        return q;
    }

    private Map<String, Object> getMockNodeQuestion(int i) {
        Map<String, Object> q = new HashMap<>();
        q.put("category", "Technical Theory");
        q.put("domain", "Node.js");
        q.put("difficulty", "Medium");
        q.put("evaluationCriteria", List.of("Accuracy"));
        q.put("qualityScore", 90);
        q.put("questionFormat", "mcq");
        q.put("tags", List.of("Node"));

        if (i % 2 == 0) {
            q.put("question", "Which module in Node.js is used to create an HTTP server?");
            q.put("options", List.of("fs", "http", "path", "crypto"));
            q.put("correctOption", "http");
            q.put("expectedTopics", List.of("HTTP"));
        } else {
            q.put("question", "Which of the following describes the Node.js event loop?");
            q.put("options", List.of("Multi-threaded", "Blocking I/O", "Single-threaded Non-blocking", "Synchronous execution"));
            q.put("correctOption", "Single-threaded Non-blocking");
            q.put("expectedTopics", List.of("Event Loop"));
        }
        return q;
    }

    private Map<String, Object> getMockJavaQuestion(int i) {
        Map<String, Object> q = new HashMap<>();
        q.put("category", "Technical Theory");
        q.put("domain", "Java");
        q.put("difficulty", "Medium");
        q.put("evaluationCriteria", List.of("Accuracy"));
        q.put("qualityScore", 90);
        q.put("questionFormat", "mcq");
        q.put("tags", List.of("Java"));

        if (i % 2 == 0) {
            q.put("question", "Which collection allows duplicate elements?");
            q.put("options", List.of("Set", "HashSet", "List", "TreeSet"));
            q.put("correctOption", "List");
            q.put("expectedTopics", List.of("Collections"));
        } else {
            q.put("question", "Which keyword prevents a method from being overridden?");
            q.put("options", List.of("static", "final", "const", "volatile"));
            q.put("correctOption", "final");
            q.put("expectedTopics", List.of("OOPs"));
        }
        return q;
    }

    private Map<String, Object> getMockBackendQuestion(int i) {
        Map<String, Object> q = new HashMap<>();
        q.put("category", "Technical Theory");
        q.put("domain", "Backend");
        q.put("difficulty", "Medium");
        q.put("evaluationCriteria", List.of("Accuracy"));
        q.put("qualityScore", 90);
        q.put("questionFormat", "mcq");
        q.put("tags", List.of("Backend"));

        if (i % 2 == 0) {
            q.put("question", "What is the purpose of JWT?");
            q.put("options", List.of("Database Storage", "Authentication", "CSS Styling", "Logging"));
            q.put("correctOption", "Authentication");
            q.put("expectedTopics", List.of("Security"));
        } else {
            q.put("question", "Which HTTP method is idempotent and used to replace a resource completely?");
            q.put("options", List.of("POST", "PUT", "PATCH", "DELETE"));
            q.put("correctOption", "PUT");
            q.put("expectedTopics", List.of("REST APIs"));
        }
        return q;
    }

    private Map<String, Object> getMockFullStackQuestion(int i) {
        Map<String, Object> q = new HashMap<>();
        q.put("category", "Technical Theory");
        q.put("domain", "Full Stack");
        q.put("difficulty", "Medium");
        q.put("evaluationCriteria", List.of("Accuracy"));
        q.put("qualityScore", 90);
        q.put("questionFormat", "mcq");
        q.put("tags", List.of("Full Stack"));

        if (i % 2 == 0) {
            q.put("question", "Which is an OS scheduling algorithm?");
            q.put("options", List.of("Round Robin", "Dijkstra", "Bubble Sort", "Binary Search"));
            q.put("correctOption", "Round Robin");
            q.put("expectedTopics", List.of("OS"));
        } else {
            q.put("question", "Which of these is NOT an ACID property?");
            q.put("options", List.of("Atomicity", "Consistency", "Isolation", "Distribution"));
            q.put("correctOption", "Distribution");
            q.put("expectedTopics", List.of("DBMS"));
        }
        return q;
    }

    private Map<String, Object> getMockGeneralQuestion(String domain, int i) {
        Map<String, Object> q = new HashMap<>();
        q.put("category", "Technical Theory");
        q.put("domain", domain.isEmpty() ? "General" : domain);
        q.put("difficulty", "Medium");
        q.put("expectedTopics", List.of("Databases"));
        q.put("evaluationCriteria", List.of("Accuracy"));
        q.put("hints", List.of("Think about core architectural design principles."));
        q.put("qualityScore", 90);
        q.put("questionFormat", "mcq");
        q.put("question", "What is the primary difference between a relational database and a non-relational database?");
        q.put("options", List.of("Relational has schema, non-relational is schema-less", "Non-relational is always faster", "Relational doesn't support SQL", "Non-relational is only for cloud"));
        q.put("correctOption", "Relational has schema, non-relational is schema-less");
        q.put("tags", List.of("General", "Technical Theory"));
        return q;
    }
}
