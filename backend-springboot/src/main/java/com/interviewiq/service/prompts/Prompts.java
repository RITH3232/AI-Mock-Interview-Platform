package com.interviewiq.service.prompts;

public class Prompts {

    public static final String RESUME_ANALYSIS_PROMPT = """
You are an expert AI Technical Recruiter and Career Coach.\s
Analyze the provided resume text and extract structured information, calculate scores, and provide actionable feedback.

Resume Text:
\"\"\"
{{RESUME_TEXT}}
\"\"\"

Your task is to return a JSON object with the following structure strictly. Do not include any markdown formatting, just the raw JSON object.

{
  "resumeSummary": "<concise AI summary of the candidate profile>",
  "extractedSkills": ["skill1", "skill2"],
  "extractedProjects": [
    {
      "name": "Project Name",
      "technologies": ["tech1", "tech2"],
      "complexity": "Beginner | Intermediate | Advanced",
      "readinessScore": <number 0-100>
    }
  ],
  "extractedExperience": ["role1 at company1"],
  "extractedEducation": ["degree at university"],
  "extractedCertifications": ["cert1"],
 \s
  "atsScore": <number 0-100>,
  "resumeScore": <number 0-100>,
  "industryReadinessScore": <number 0-100>,
  "keywordMatchScore": <number 0-100>,
  "projectQualityScore": <number 0-100>,
  "experienceScore": <number 0-100>,
  "technicalSkillScore": <number 0-100>,
  "formattingScore": <number 0-100>,

  "strengths": ["strength1", "strength2"],
  "weaknesses": ["weakness1", "weakness2"],
  "recommendations": ["rec1", "rec2"],
  "missingSkills": ["missingSkill1", "missingSkill2"],
  "missingTechnologies": ["missingTech1", "missingTech2"]
}

Evaluate scores based on industry standards for a Software Engineer role. Be critical and realistic.
""";

    public static final String BASE_JSON_INSTRUCTION = """
You must strictly return a valid JSON object matching this schema. Do not return markdown, do not wrap in ```json. 

{
  "questions": [
    {
      "question": "string",
      "category": "Technical Theory" | "Coding Concepts" | "Behavioral" | "Aptitude" | "Project Discussion" | "Resume Discussion" | "Scenario-Based" | "DSA",
      "domain": "string",
      "difficulty": "Easy" | "Medium" | "Hard",
      "expectedTopics": ["string"],
      "evaluationCriteria": ["string"],
      "hints": ["string"],
      "qualityScore": number (0-100),
      "tags": ["string"],
      "questionFormat": "text" | "code" | "mcq",
      "options": ["string"] (only if questionFormat is mcq),
      "correctOption": "string" (only if questionFormat is mcq),
      "testCases": [{"input": "string", "expectedOutput": "string"}] (only if questionFormat is code),
      "starterCode": "string" (only if questionFormat is code),
      "languageOptions": ["string"] (only if questionFormat is code)
    }
  ]
}
""";

    public static final String DSA_PROMPT = """
Generate {{COUNT}} Data Structures and Algorithms (DSA) coding interview questions at a '{{DIFFICULTY}}' difficulty level for a candidate with '{{EXPERIENCE}}' experience.
Ensure the questions are realistic and challenging.
The questions MUST cover topics like Arrays, Strings, Linked Lists, Trees, Graphs, Dynamic Programming, etc.

CRITICAL FORMATTING RULE:
You MUST return coding questions (questionFormat: 'code') with 'testCases' and 'starterCode'. Do not return MCQs or text questions.

{{JSON_INSTRUCTION}}
""";

    public static final String REACT_PROMPT = """
Generate {{COUNT}} React.js technical interview questions at a '{{DIFFICULTY}}' difficulty level for a candidate with '{{EXPERIENCE}}' experience.

CRITICAL FORMATTING RULE:
You MUST return Scenario-Based MCQs (questionFormat: 'mcq') focusing on performance optimization, hooks, component lifecycle, and state management scenarios. Provide exactly 4 'options' and 1 'correctOption'.

{{JSON_INSTRUCTION}}
""";

    public static final String NODE_PROMPT = """
Generate {{COUNT}} Node.js technical interview questions at a '{{DIFFICULTY}}' difficulty level for a candidate with '{{EXPERIENCE}}' experience.

CRITICAL FORMATTING RULE:
You MUST return Scenario-Based MCQs (questionFormat: 'mcq') focusing on middleware, event loop, architecture, and core modules. Provide exactly 4 'options' and 1 'correctOption'.

{{JSON_INSTRUCTION}}
""";

    public static final String JAVA_PROMPT = """
Generate {{COUNT}} Java technical interview questions at a '{{DIFFICULTY}}' difficulty level for a candidate with '{{EXPERIENCE}}' experience.

CRITICAL FORMATTING RULE:
You MUST return Theory MCQs (questionFormat: 'mcq') focusing on OOPs, Collections, Exception Handling, Multithreading, JVM, and Streams API. Provide exactly 4 'options' and 1 'correctOption'.

{{JSON_INSTRUCTION}}
""";

    public static final String BACKEND_PROMPT = """
Generate {{COUNT}} Backend technical interview questions at a '{{DIFFICULTY}}' difficulty level for a candidate with '{{EXPERIENCE}}' experience.

CRITICAL FORMATTING RULE:
You MUST return Scenario-Based MCQs (questionFormat: 'mcq') focusing on REST APIs, Authentication, JWT, Databases, Caching, and Security. Provide exactly 4 'options' and 1 'correctOption'.

{{JSON_INSTRUCTION}}
""";

    public static final String FULLSTACK_PROMPT = """
Generate {{COUNT}} Full Stack technical interview questions at a '{{DIFFICULTY}}' difficulty level for a candidate with '{{EXPERIENCE}}' experience.

CRITICAL FORMATTING RULE:
You MUST return a mix of Frontend MCQs, Backend MCQs, Database MCQs, API Design, and CS Fundamentals (Operating Systems, DBMS, Computer Networks, OOPs). Use 'mcq' format strictly with exactly 4 'options' and 1 'correctOption'.

{{JSON_INSTRUCTION}}
""";

    public static final String APTITUDE_PROMPT = """
You are an expert Aptitude and Logical Reasoning examiner.
Generate {{COUNT}} Aptitude test questions for the topic/domain '{{DOMAIN}}' at a '{{DIFFICULTY}}' difficulty level.

CRITICAL RULES — READ CAREFULLY:
1. EVERY question MUST be an aptitude, logical reasoning, verbal ability, data interpretation, or puzzle question. No exceptions.
2. Do NOT generate any programming, coding, DSA, React, Java, Backend, or other technical questions.
3. All questions MUST be highly relevant to the specified topic/domain: '{{DOMAIN}}'.
4. Expected Aptitude Topics: Percentages, Profit and Loss, Time and Work, Time, Speed and Distance, Probability, Permutations and Combinations, Ratios and Proportions, Averages, Number Systems, Logical Reasoning, Blood Relations, Coding-Decoding, Puzzles, Data Interpretation.

CRITICAL FORMATTING RULE:
You MUST return MCQs (questionFormat: 'mcq') with exactly 4 'options' and 1 'correctOption'.
Every question's "category" MUST be "Aptitude".
Every question's "expectedTopics" MUST strictly match the specific aptitude topic (e.g. ["Profit and Loss"] or ["Puzzles"]).

{{JSON_INSTRUCTION}}
""";

    public static final String COMPANY_APTITUDE_PROMPT = """
Generate {{COUNT}} Aptitude test questions for a candidate interviewing at {{COMPANY}}.
Company Interview Style: {{STYLE}}
Focus Areas: {{FOCUS_AREAS}}
Candidate Experience: {{EXPERIENCE}}
Topic/Domain: {{DOMAIN}}

CRITICAL RULES — READ CAREFULLY:
1. EVERY question MUST be an aptitude, logical reasoning, verbal ability, data interpretation, or puzzle question. No exceptions.
2. Do NOT generate any programming, coding, DSA, React, Java, Backend, or other technical questions.
3. All questions MUST be highly relevant to the specified topic/domain: '{{DOMAIN}}'.
4. Expected Aptitude Topics: Percentages, Profit and Loss, Time and Work, Time, Speed and Distance, Probability, Permutations and Combinations, Ratios and Proportions, Averages, Number Systems, Logical Reasoning, Blood Relations, Coding-Decoding, Puzzles, Data Interpretation.

CRITICAL FORMATTING RULE:
You MUST return MCQs (questionFormat: 'mcq') with exactly 4 'options' and 1 'correctOption'.
Every question's "category" MUST be "Aptitude".
Every question's "expectedTopics" MUST strictly match the specific aptitude topic (e.g. ["Profit and Loss"] or ["Puzzles"]).

{{JSON_INSTRUCTION}}
""";

    public static final String TECHNICAL_PROMPT = """
Generate {{COUNT}} technical interview questions for the domain '{{DOMAIN}}' at a '{{DIFFICULTY}}' difficulty level for a candidate with '{{EXPERIENCE}}' experience.
Ensure the questions are realistic and challenging.

{{JSON_INSTRUCTION}}
""";

    public static final String PERSONALIZED_PROMPT = """
You are an expert technical interviewer building a fully personalized mock interview.

Candidate Profile:
- Role: {{ROLE}}
- Skills selected by the candidate: {{SKILLS}}
- Experience Level: {{EXPERIENCE}}
- Difficulty: {{DIFFICULTY}}
- Interview Type: {{INTERVIEW_TYPE}}
{{COMPANY_CONTEXT}}

Generate exactly {{COUNT}} interview questions.

CRITICAL RELEVANCE RULES — READ CAREFULLY:
1. EVERY question MUST be directly derived from the Role and the selected Skills above. Do NOT include unrelated topics that are not implied by the role or skills.
2. If skills include several distinct technologies (e.g. "Java", "OOP", "Spring Boot"), spread the questions across them roughly evenly instead of over-focusing on just one.
3. Calibrate depth and phrasing to the stated Experience Level and Difficulty.

Interview Type Guidance (follow the one that matches {{INTERVIEW_TYPE}}):
- Technical: hands-on and theory questions grounded in the listed skills. Use 'mcq' or 'code' format where a skill naturally supports it (e.g. DSA, coding-heavy skills), otherwise 'text'.
- HR or Behavioral: soft-skills and situational/behavioral questions calibrated to the role and seniority (conflict resolution, ownership, collaboration, real-world scenarios). Use 'text' format only. Do not generate generic questions like "What are your strengths?" — ground every question in the candidate's role and skills.
- System Design: architecture, scalability, and trade-off questions that use the listed skills as system components/building blocks. Use 'text' format only.
- Mixed: produce a balanced blend — roughly half technical (grounded in skills), the remainder split between behavioral and system-design/problem-solving questions appropriate to the experience level.

{{JSON_INSTRUCTION}}
""";

    public static final String RESUME_PROMPT = """
Based on the following candidate resume, generate {{COUNT}} targeted, personalized interview questions at '{{DIFFICULTY}}' difficulty level.
The questions must prioritize resume content over generic templates.

Generate questions matching this approximate distribution:
1. 60-70% Resume-specific questions: Diving into the candidate's actual projects, technologies used, architecture decisions, challenges faced, certifications, or experience listed in their resume.
2. 20-30% Domain-specific questions: Grounded in their technical field (e.g. Java Developer, React Developer, Backend Developer), prioritizing mentioned technologies over unrelated ones.
3. 10-20% General behavioral questions: Grounded in their actual listed projects or experience (e.g., how they handled conflicts, scope changes, or deadlines on those projects).

CRITICAL REQUIREMENTS:
- Every question must be grounded in the candidate's uploaded resume.
- If a project is mentioned in the resume, at least one question MUST reference that project directly by name.
- If a technology is mentioned in the resume, prioritize questions on that technology over unrelated technologies.
- Do not always generate the exact same set of questions; introduce slight variations or focus areas while keeping all questions strictly grounded in their resume.

Resume Details:
- Skills & Tech Stack: {{SKILLS}}
- Projects: {{PROJECTS}}
- Experience: {{EXPERIENCE}}
- Education: {{EDUCATION}}
- Certifications: {{CERTIFICATIONS}}

CRITICAL FORMATTING RULE:
You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.
Every question's "category" MUST be "Resume Discussion".

{{JSON_INSTRUCTION}}
""";

    public static final String COMPANY_PROMPT = """
Generate {{COUNT}} interview questions for a candidate interviewing at {{COMPANY}}.
Company Interview Style: {{STYLE}}
Focus Areas: {{FOCUS_AREAS}}
Candidate Experience: {{EXPERIENCE}}
Domain: {{DOMAIN}}

{{JSON_INSTRUCTION}}
""";

    public static final String FOLLOWUP_PROMPT = """
The candidate was asked: "{{ORIGINAL_QUESTION}}".
Their answer was evaluated as weak or incomplete.
Generate 1 adaptive follow-up question that prompts them to explain further, provide an example, or discuss limitations/alternatives.

You must strictly return a valid JSON object:
{
  "question": "string",
  "category": "string",
  "domain": "string",
  "difficulty": "string",
  "expectedTopics": ["string"],
  "evaluationCriteria": ["string"],
  "hints": ["string"],
  "qualityScore": number,
  "tags": ["string"]
}
""";

    public static final String HR_SOFTWARE_ENGINEERING_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Software Engineering)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- How the candidate handles conflicts with other engineers regarding implementation approaches (e.g., system design disagreements, technology stack selection).
- How the candidate copes with changing requirements during a sprint or project timeline.
- Real production issue resolution, post-mortem processes, and what was learned from major failures.
- Balancing technical debt against meeting shipping deadlines.
- Collaboration with product managers, QA, and other engineering teams.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Software Engineering. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Engineering Best Practices".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Focus on a concrete software engineering scenario you have worked on".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_FULLSTACK_DEVELOPER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Full Stack Developer)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Balancing frontend and backend development tasks under tight deadlines.
- Managing complex user features end-to-end, from the database schema to the user interface.
- Handling disagreements on API contract designs between frontend and backend requirements.
- Resolving integration issues where the root cause might be obscure or span across multiple stack layers.
- Collaborative styles when working with UI designers, QA engineers, and operations teams.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Full Stack Development. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Full Stack Integration".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Provide a real example of a feature you delivered end-to-end".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_FRONTEND_DEVELOPER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Frontend Developer)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Resolving communication hurdles and technical limitations when collaborating with UI/UX designers.
- Handling user interface consistency across different browsers and responsive devices under pressure.
- Responding to critical or negative feedback on a UI component or layout from stakeholders or users.
- Adapting to rapidly changing frontend ecosystems and frameworks in the middle of a project.
- Optimizing visual performance and page loading speed, while managing trade-offs.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Frontend Development. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "UX Empathy".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Focus on user experience and styling collaboration".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_BACKEND_DEVELOPER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Backend Developer)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Resolving performance bottlenecks and optimizing slow API endpoints or database queries under load.
- Designing and handling challenges in scalable backend systems (e.g. queue workers, distributed locks, microservices).
- Diagnosing database issues such as lockups, connection pool exhaustion, or replication lag in production.
- Mitigating security issues, data integrity failures, or system reliability breakdowns.
- Communicating system architecture tradeoffs and API contracts with frontend teams.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Backend Development. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "System Scaling & Reliability".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Describe a time you optimized a slow API or handled database performance issues".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_JAVA_DEVELOPER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Java Developer)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Discovering, debugging, and resolving memory leaks (e.g., GC tuning, OutOfMemoryError) or thread deadlocks in Java applications.
- Working through challenging Spring/Spring Boot issues (e.g. beans cycle dependencies, transaction management, filter chain issues).
- Improving and enforcing Java code quality, clean architecture, and API design in a team.
- Migrating JVM versions or upgrading core frameworks (e.g. Spring Boot 2.x to 3.x) while dealing with backward compatibility issues.
- Teamwork and engineering practices surrounding JUnit/Mockito testing and CI/CD pipelines in Java environments.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Java Development. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Java Ecosystem Best Practices".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Focus on a Java-specific runtime or debugging challenge you resolved".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_REACT_DEVELOPER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (React Developer)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Resolving complex frontend state management problems (e.g., Redux store complexity, Context API render optimization).
- Tackling React performance bottlenecks, unnecessary re-renders, layout shifts, or lazy loading challenges in high-traffic applications.
- Implementing tricky component architectures and communication patterns (e.g., render props, compound components).
- Aligning UI implementations with designers and dealing with visual details and browser support.
- Refactoring legacy class components or class-based architectures into modern functional hooks.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to React Development. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "React Best Practices".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Focus on a state management, performance, or component architecture challenge in React".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_NODEJS_DEVELOPER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Node.js Developer)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Dealing with performance issues due to event-loop blocking operations or memory leaks in Node.js processes.
- Designing modular and clean middleware logic for request handling, authentication, and logging.
- Orchestrating asynchronous operations (e.g. streaming, file processing, promises/async-await) safely in high-concurrency environments.
- Handling security vulnerabilities, input validation, and secure session management.
- Collaborative backend coordination (e.g. microservices orchestration, third-party API dependencies).

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Node.js Development. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Node.js Performance & Reliability".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Describe a time when you solved an event loop or async handling issue in a Node.js project".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_DATA_ANALYST_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Data Analyst)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Resolving challenges with dirty, unstructured, or incomplete datasets under time constraints.
- Managing and resolving conflicts when stakeholders disagree with data insights or report findings.
- Translating complex data patterns into actionable business opportunities or clear narratives for non-technical stakeholders.
- Handling priority shifts when multiple product or business teams request reports concurrently.
- Validating data accuracy and catching bugs in SQL scripts or ETL pipelines before reports are delivered.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Data Analysis. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Analytical Integrity & Business Impact".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Give a real example where data analysis influenced a business decision or resolved a stakeholder conflict".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_DATA_SCIENTIST_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Data Scientist)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Debugging issues when a machine learning model works perfectly in training/cross-validation but performs poorly in production.
- Explaining complex, "black-box" model outputs (like deep neural networks or ensemble models) to skeptical, non-technical business leaders.
- Tackling features engineering bottlenecks or managing datasets with extreme imbalances or lack of labeled samples.
- Deciding on technical trade-offs between model complexity (e.g. execution speed, maintenance overhead) and model accuracy.
- Designing experimental setups (like A/B testing) and handling inconclusive results or statistical anomalies.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Data Science. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Statistical Rigor & Model Deployment".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Explain how you balanced complex algorithms with practical business explanations".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_DEVOPS_ENGINEER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (DevOps Engineer)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Responding to a major production outage, coordinating hotfixes, communicating status under high pressure, and managing post-mortems.
- Resolving conflicts when developers bypass CI/CD security, code quality gates, or testing checks.
- Migrating legacy, manual deployments into structured, automated Infrastructure as Code (IaC) under tight constraints.
- Managing drift in cloud configurations and maintaining consistency across dev, staging, and production environments.
- Designing security/compliance configurations and dealing with secret leakage in deployment files.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to DevOps and Site Reliability Engineering. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Infrastructure Automation & Outage Recovery".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Focus on how you coordinated team actions during an outage or automated a painful deployment".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_QA_ENGINEER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (QA Engineer)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Finding a major blocker bug right before a production release and managing the communication with product and development teams.
- Resolving conflicts when a developer disagrees with a bug report, claiming it is "minor", "expected behavior", or "not a bug".
- Structuring and balancing automated regression test suites versus time-consuming manual QA testing under crunch deadlines.
- Designing test plans and coverage for features that have vague, shifting, or completely undocumented requirements.
- Post-incident analysis when a critical bug slips into production, and refining testing processes to prevent recurrences.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Quality Assurance and Testing. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Quality Advocacy & Test Automation".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Give a real example of defending quality standards or coordinating bug triages under pressure".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_PRODUCT_MANAGER_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}} (Product Manager)
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Resolving prioritization conflicts and saying "no" to important, demanding stakeholders or executives.
- Launching a product or feature that did not meet target metrics, conducting a retrospective, and deciding on a pivot.
- Resolving disagreements between engineering estimation timelines and business release pressure.
- Handling situations where user feedback or usability studies completely contradict your initial product roadmap.
- Defining, tracking, and communicating key performance indicators (KPIs) to align cross-functional engineering and business teams.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to Product Management. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method", "Stakeholder Alignment & Product Strategy".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Share a specific product launch, stakeholder dispute, or data-driven prioritization scenario".

{{JSON_INSTRUCTION}}
""";

    public static final String HR_DEFAULT_TEMPLATE = """
You are an expert HR Interviewer. Generate {{COUNT}} behavioral HR interview questions specifically for the selected domain: {{DOMAIN}}.
Every question must relate to real-world situations encountered in that profession. Do not generate generic HR questions.

Interview Details:
- Domain: {{DOMAIN}}
- Difficulty Level: {{DIFFICULTY}}
- Experience Level: {{EXPERIENCE}}
- Interview Type: HR / Behavioral

Role-Specific Behavioral Expectations:
- Working through professional challenges and responsibilities specific to a {{DOMAIN}} role.
- Collaborating in cross-functional teams, solving conflicts, and building alignment.
- Overcoming setbacks, failures, or tight schedules in {{DOMAIN}} projects.
- Exhibiting continuous learning, adaptability, and domain growth.

CRITICAL RULES:
1. EVERY question MUST be a behavioral or situational question tailored to the {{DOMAIN}} role. No exceptions.
2. Do NOT generate generic HR questions like "What are your strengths?", "Where do you see yourself in 5 years?", or "Tell me about yourself" unless explicitly requested.
3. Every question must be open-ended, asking for real-world scenarios or challenges.
4. You MUST return open-ended text questions (questionFormat: 'text'). Do NOT return MCQs or coding questions.

Every question's "category" MUST be "Behavioral".
Every question's "evaluationCriteria" MUST include: "Communication", "Clarity", "Relevance", "Use of STAR Method".
Every question's "hints" MUST include: "Use the STAR method: Situation, Task, Action, Result", "Describe a real example from your experience in the {{DOMAIN}} domain".

{{JSON_INSTRUCTION}}
""";

    public static final String EVALUATION_PROMPT = """
You are an expert AI Interviewer evaluating a candidate's answer.

Question: "{{QUESTION}}"
Candidate's Answer: "{{ANSWER}}"
Duration Taken: {{DURATION}} seconds

Evaluate the answer strictly based on these dimensions:
1. Technical Accuracy (0-100): Correctness, depth, terminology.
2. Communication (0-100): Clarity, structure, professionalism.
3. Completeness (0-100): Missing concepts, examples, coverage against expected topics: {{EXPECTED_TOPICS}}.
4. Confidence (0-100): Answer certainty (inferred from phrasing/structure).
5. Problem Solving (0-100): Reasoning, approach, thought process.
6. Domain Knowledge (0-100): Knowledge specific to {{DOMAIN}}.

CRITICAL SCORING INSTRUCTIONS:
- DO NOT default to scores in the 80-90 range. You must use the full 0-100 spectrum.
- If the answer is completely wrong or "I don't know", the score MUST be 0-20.
- If the answer is partially correct but lacks depth, the score MUST be 40-60.
- Only award 90+ if the answer is exceptionally detailed, technically flawless, and covers all expected topics.

Identify strengths, weaknesses, missing concepts, and actionable recommendations.

You MUST return the evaluation strictly as a valid JSON object matching this exact schema:
{
  "technicalScore": number,
  "communicationScore": number,
  "completenessScore": number,
  "confidenceScore": number,
  "problemSolvingScore": number,
  "domainKnowledgeScore": number,
  "strengths": ["string"],
  "weaknesses": ["string"],
  "missingConcepts": ["string"],
  "recommendations": ["string"]
}
""";

    public static final String ROADMAP_PROMPT = """
Based on the candidate's performance across the entire interview session, generate a personalized learning roadmap.

Weaknesses Observed: {{WEAKNESSES}}
Missing Concepts: {{MISSING_CONCEPTS}}

You MUST return strictly a JSON object:
{
  "learningRoadmap": ["string (actionable steps)"]
}
""";

    public static final String CAREER_READINESS_PROMPT = """
You are an expert AI Career Coach evaluating a candidate's overall readiness based on an interview session.

Overall Score: {{OVERALL_SCORE}}/100
Technical Score: {{TECHNICAL_SCORE}}/100
Problem Solving Score: {{PROBLEM_SOLVING_SCORE}}/100
Domain Knowledge Score: {{DOMAIN_SCORE}}/100
Experience Level: {{EXPERIENCE_LEVEL}}

Determine the candidate's Career Readiness Level. You MUST output one of these exact levels:
"Beginner", "Developing", "Interview Ready", "Strong Candidate", "Top Performer"

You MUST return strictly a JSON object:
{
  "careerReadinessLevel": "string"
}
""";
}
