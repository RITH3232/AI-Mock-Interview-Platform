package com.interviewiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.service.prompts.Prompts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class PromptBuilderService {

    private JsonNode companiesConfig;

    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getResourceAsStream("/companies.json")) {
            if (is != null) {
                ObjectMapper mapper = new ObjectMapper();
                companiesConfig = mapper.readTree(is);
                log.info("Successfully loaded companies.json configuration.");
            } else {
                log.warn("companies.json not found in resources path.");
            }
        } catch (Exception e) {
            log.error("Failed to load companies.json", e);
        }
    }

    public String buildTechnicalPrompt(String domain, String difficulty, String experience, int count, String interviewType) {
        String baseTemplate = Prompts.TECHNICAL_PROMPT;

        if ("DSA".equalsIgnoreCase(domain)) {
            baseTemplate = Prompts.DSA_PROMPT;
        } else if ("React".equalsIgnoreCase(domain)) {
            baseTemplate = Prompts.REACT_PROMPT;
        } else if ("Node.js".equalsIgnoreCase(domain)) {
            baseTemplate = Prompts.NODE_PROMPT;
        } else if ("Java".equalsIgnoreCase(domain)) {
            baseTemplate = Prompts.JAVA_PROMPT;
        } else if ("Backend".equalsIgnoreCase(domain)) {
            baseTemplate = Prompts.BACKEND_PROMPT;
        } else if ("Full Stack".equalsIgnoreCase(domain)) {
            baseTemplate = Prompts.FULLSTACK_PROMPT;
        } else if ("Aptitude".equalsIgnoreCase(interviewType) || "Aptitude".equalsIgnoreCase(domain) || 
                   domain.toLowerCase().contains("aptitude") || domain.toLowerCase().contains("reasoning")) {
            baseTemplate = Prompts.APTITUDE_PROMPT;
        }

        String prompt = baseTemplate
                .replace("{{DOMAIN}}", domain)
                .replace("{{DIFFICULTY}}", difficulty)
                .replace("{{EXPERIENCE}}", experience)
                .replace("{{COUNT}}", String.valueOf(count));

        return prompt.replace("{{JSON_INSTRUCTION}}", Prompts.BASE_JSON_INSTRUCTION);
    }

    public String buildResumePrompt(
            List<String> skills,
            List<String> projects,
            List<String> experience,
            List<String> education,
            List<String> certifications,
            String difficulty,
            int count
    ) {
        String prompt = Prompts.RESUME_PROMPT
                .replace("{{SKILLS}}", !skills.isEmpty() ? String.join(", ", skills) : "None")
                .replace("{{PROJECTS}}", !projects.isEmpty() ? String.join(", ", projects) : "None")
                .replace("{{EXPERIENCE}}", !experience.isEmpty() ? String.join(", ", experience) : "None")
                .replace("{{EDUCATION}}", !education.isEmpty() ? String.join(", ", education) : "None")
                .replace("{{CERTIFICATIONS}}", !certifications.isEmpty() ? String.join(", ", certifications) : "None")
                .replace("{{DIFFICULTY}}", difficulty)
                .replace("{{COUNT}}", String.valueOf(count));

        return prompt.replace("{{JSON_INSTRUCTION}}", Prompts.BASE_JSON_INSTRUCTION);
    }

    public String buildCompanyPrompt(String companyName, String domain, String experience, int count, String interviewType) {
        String style = "Standard technical interview covering fundamentals and problem solving.";
        List<String> focusList = List.of("Problem Solving", "Core Domain Concepts", "Practical Application");

        if (companiesConfig != null) {
            String normalizedCompany = companyName.trim().toLowerCase();
            Iterator<String> fieldNames = companiesConfig.fieldNames();
            String matchedKey = null;
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                if (key.toLowerCase().equals(normalizedCompany)) {
                    matchedKey = key;
                    break;
                }
            }

            if (matchedKey != null) {
                JsonNode config = companiesConfig.get(matchedKey);
                style = config.path("interviewStyle").asText(style);
                JsonNode focusNode = config.path("focusAreas");
                if (focusNode.isArray()) {
                    List<String> list = new ArrayList<>();
                    focusNode.forEach(n -> list.add(n.asText()));
                    focusList = list;
                }
            }
        }

        String baseTemplate = "Aptitude".equalsIgnoreCase(interviewType) ? Prompts.COMPANY_APTITUDE_PROMPT : Prompts.COMPANY_PROMPT;

        String prompt = baseTemplate
                .replace("{{COMPANY}}", companyName)
                .replace("{{STYLE}}", style)
                .replace("{{FOCUS_AREAS}}", String.join(", ", focusList))
                .replace("{{EXPERIENCE}}", experience)
                .replace("{{DOMAIN}}", domain)
                .replace("{{COUNT}}", String.valueOf(count));

        return prompt.replace("{{JSON_INSTRUCTION}}", Prompts.BASE_JSON_INSTRUCTION);
    }

    public String buildHRPrompt(String domain, String difficulty, String experience, int count) {
        String baseTemplate = Prompts.HR_DEFAULT_TEMPLATE;
        String normalizedDomain = domain == null ? "" : domain.trim().toLowerCase();

        if (normalizedDomain.contains("software engineering") || normalizedDomain.contains("software engineer")) {
            baseTemplate = Prompts.HR_SOFTWARE_ENGINEERING_TEMPLATE;
        } else if (normalizedDomain.contains("full stack") || normalizedDomain.contains("fullstack")) {
            baseTemplate = Prompts.HR_FULLSTACK_DEVELOPER_TEMPLATE;
        } else if (normalizedDomain.contains("frontend") || normalizedDomain.contains("front end")) {
            baseTemplate = Prompts.HR_FRONTEND_DEVELOPER_TEMPLATE;
        } else if (normalizedDomain.contains("backend") || normalizedDomain.contains("back end")) {
            baseTemplate = Prompts.HR_BACKEND_DEVELOPER_TEMPLATE;
        } else if (normalizedDomain.contains("java")) {
            baseTemplate = Prompts.HR_JAVA_DEVELOPER_TEMPLATE;
        } else if (normalizedDomain.contains("react")) {
            baseTemplate = Prompts.HR_REACT_DEVELOPER_TEMPLATE;
        } else if (normalizedDomain.contains("node")) {
            baseTemplate = Prompts.HR_NODEJS_DEVELOPER_TEMPLATE;
        } else if (normalizedDomain.contains("data analyst")) {
            baseTemplate = Prompts.HR_DATA_ANALYST_TEMPLATE;
        } else if (normalizedDomain.contains("data scientist") || normalizedDomain.contains("data science")) {
            baseTemplate = Prompts.HR_DATA_SCIENTIST_TEMPLATE;
        } else if (normalizedDomain.contains("devops")) {
            baseTemplate = Prompts.HR_DEVOPS_ENGINEER_TEMPLATE;
        } else if (normalizedDomain.contains("qa") || normalizedDomain.contains("quality assurance") || normalizedDomain.contains("testing")) {
            baseTemplate = Prompts.HR_QA_ENGINEER_TEMPLATE;
        } else if (normalizedDomain.contains("product manager") || normalizedDomain.equals("pm")) {
            baseTemplate = Prompts.HR_PRODUCT_MANAGER_TEMPLATE;
        }

        String prompt = baseTemplate
                .replaceAll("\\{\\{DOMAIN\\}\\}", domain != null ? domain : "Software Engineering")
                .replace("{{DIFFICULTY}}", difficulty != null ? difficulty : "Medium")
                .replace("{{EXPERIENCE}}", experience != null ? experience : "Internship")
                .replace("{{COUNT}}", String.valueOf(count));
        return prompt.replace("{{JSON_INSTRUCTION}}", Prompts.BASE_JSON_INSTRUCTION);
    }

    public String buildFollowUpPrompt(String originalQuestion) {
        return Prompts.FOLLOWUP_PROMPT.replace("{{ORIGINAL_QUESTION}}", originalQuestion);
    }

    public String buildPersonalizedPrompt(
            String role,
            List<String> skills,
            String difficulty,
            String experience,
            String interviewType,
            int count,
            String companyType,
            String targetCompany
    ) {
        String companyContext = resolveCompanyContext(companyType, targetCompany);

        String prompt = Prompts.PERSONALIZED_PROMPT
                .replace("{{ROLE}}", role != null ? role : "Software Engineer")
                .replace("{{SKILLS}}", (skills != null && !skills.isEmpty()) ? String.join(", ", skills) : role != null ? role : "General")
                .replace("{{DIFFICULTY}}", difficulty != null ? difficulty : "Medium")
                .replace("{{EXPERIENCE}}", experience != null ? experience : "Fresher")
                .replace("{{INTERVIEW_TYPE}}", interviewType != null ? interviewType : "Technical")
                .replace("{{COUNT}}", String.valueOf(count))
                .replace("{{COMPANY_CONTEXT}}", companyContext);

        return prompt.replace("{{JSON_INSTRUCTION}}", Prompts.BASE_JSON_INSTRUCTION);
    }

    private String resolveCompanyContext(String companyType, String targetCompany) {
        if (targetCompany != null && !targetCompany.isBlank()) {
            String style = "Standard technical interview covering fundamentals and problem solving.";
            List<String> focusList = List.of("Problem Solving", "Core Domain Concepts", "Practical Application");

            if (companiesConfig != null) {
                String normalized = targetCompany.trim().toLowerCase();
                Iterator<String> fieldNames = companiesConfig.fieldNames();
                String matchedKey = null;
                while (fieldNames.hasNext()) {
                    String key = fieldNames.next();
                    if (key.toLowerCase().equals(normalized)) {
                        matchedKey = key;
                        break;
                    }
                }
                if (matchedKey != null) {
                    JsonNode config = companiesConfig.get(matchedKey);
                    style = config.path("interviewStyle").asText(style);
                    JsonNode focusNode = config.path("focusAreas");
                    if (focusNode.isArray()) {
                        List<String> list = new ArrayList<>();
                        focusNode.forEach(n -> list.add(n.asText()));
                        focusList = list;
                    }
                }
            }

            return "- Target Company: " + targetCompany + "\n"
                    + "- Company Interview Style: " + style + "\n"
                    + "- Company Focus Areas: " + String.join(", ", focusList);
        }

        if (companyType != null && !companyType.isBlank()) {
            String blurb = switch (companyType.trim().toLowerCase()) {
                case "startup" -> "Pragmatic and fast-paced — favors ownership, scrappy problem solving, and versatility over deep specialization.";
                case "product company" -> "User-centric and scale-focused — favors strong fundamentals, system design, and product thinking.";
                case "service company" -> "Process and delivery-focused — favors core CS fundamentals, communication, and client-facing scenarios.";
                case "faang style" -> "Rigorous and structured — favors algorithmic depth, system design, and leadership/behavioral signals.";
                default -> "Standard technical interview covering fundamentals and problem solving.";
            };
            return "- Company Type: " + companyType + "\n- Company Interview Style: " + blurb;
        }

        return "";
    }
}
