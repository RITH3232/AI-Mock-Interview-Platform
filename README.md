# InterviewIQ

**Practice interviews. Get instant AI feedback. Land the job.**

AI-powered mock interview platform for practicing interviews, getting instant AI-driven feedback, and tracking progress over time.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-6.0-47A248?logo=mongodb&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)

## Features
-`JWT-based authentication (register/login)
-Resume upload and AI-driven resume analysis
-AI-generated interview questions (role-, resume-, and company-specific, with follow-ups)
- Live interview sessions with AI evaluation and scoring
- Interview reports exported as PDF
- Gamification: achievements and leaderboard
-  Admin dashboard with platform-wide analytics

## Tech Stack

**Backend** (`backend-springboot/`)
- Java 21, Spring Boot (Web, Security, Validation, Data MongoDB)
- MongoDB
- JWT auth (jjwt)
- OpenAI-compatible LLM API for question generation and evaluation
- PDF generation (OpenPDF / PDFBox)
- Cloudinary for file storage
- springdoc-openapi for API docs

**Frontend** (`frontend/`)
- React 19 + TypeScript, Vite
- Redux Toolkit + React Query
- Tailwind CSS
- Recharts, Monaco Editor

## Getting Started

### Backend

```bash
cd backend-springboot
mvn spring-boot:run
```

Configure via environment variables (see `src/main/resources/application.yml`):

| Variable | Description |
|---|---|
| `MONGO_URI` | MongoDB connection string |
| `JWT_SECRET` / `REFRESH_TOKEN_SECRET` | JWT signing secrets |
| `AI_BASE_URL` / `AI_API_KEY` / `AI_MODEL` | LLM API config (OpenAI-compatible; defaults to Groq) |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Cloudinary credentials |

Or run everything with Docker:

```bash
cd backend-springboot
docker-compose up
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Screenshots

| | |
|---|---|
| **Login** ![Login](screenshots/login.png) | **Register** ![Register](screenshots/register.png) |
| **Dashboard** ![Dashboard](screenshots/dashboard.png) | **Interview Type** ![Interview Type](screenshots/interviewtype.png) |
| **Company Type Interview** ![Company Type Interview](screenshots/companytypeinterview.png) | **Configure Interview** ![Configure Interview](screenshots/configureinterview.png) |
| **Interview Question** ![Interview Question](screenshots/interviewquestion.png) | **Coding Interview** ![Coding Interview](screenshots/codinginterview.png) |
| **Question-wise Transcript** ![Question-wise Transcript](screenshots/questionwisetranscript.png) | **Results** ![Results](screenshots/results.png) |

## Project Structure

```
backend-springboot/   Spring Boot API (controllers, services, models, security)
frontend/             React + TypeScript client
```
