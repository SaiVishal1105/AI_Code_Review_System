# 🤖 AI Code Review System

> Full-stack AI-powered code review using **Spring Boot + React + Groq API (Llama 3)**

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![React](https://img.shields.io/badge/React-18-blue)
![Groq](https://img.shields.io/badge/AI-Groq%20Llama%203-purple)

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔍 **AI Code Review** | Bugs, security issues, performance, suggestions via Groq Llama 3 |
| 📊 **Score Cards** | Quality, Readability, Security, Performance — all scored 0–100 |
| ✨ **AI Refactor** | One-click AI rewrites of your code |
| 📄 **PDF Reports** | Download beautiful PDF review reports |
| 🕒 **History** | All reviews stored with full history |
| 📊 **Dashboard** | Analytics, language breakdown, score trends |
| 🔐 **JWT Auth** | Secure login/register |
| 🖊️ **Monaco Editor** | VS Code-quality code editor in the browser |
| 🌐 **Multi-language** | Java, Python, JavaScript, TypeScript, C++, Go, Rust, Kotlin |

---

## 🏗️ Architecture

```
React Frontend (Vercel)
       ↓ HTTPS
Spring Boot API (Render/Railway)
       ↓
Groq AI API (Llama 3-70B — FREE tier)
       ↓
PostgreSQL (Neon — FREE tier)
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+
- Free [Groq API key](https://console.groq.com)

---

### 1. Clone & Configure Backend

```bash
cd backend
```

Edit `src/main/resources/application.properties`:
```properties
# Add your Groq API key (free at console.groq.com)
groq.api.key=gsk_YOUR_KEY_HERE
```

### 2. Run Backend

```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```

Backend runs at: **http://localhost:8080**
H2 Console: **http://localhost:8080/h2-console**

---

### 3. Run Frontend

```bash
cd frontend
npm install
npm start
```

Frontend runs at: **http://localhost:3000**

---

## 🔑 Getting Your FREE Groq API Key

1. Go to [console.groq.com](https://console.groq.com)
2. Create a free account
3. Generate an API key
4. Paste it into `application.properties`

**Free tier:** Very generous — ~14,400 requests/day with Llama 3-70B

---

## 📁 Project Structure

```
ai-code-review/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/codereview/
│       ├── AiCodeReviewApplication.java   ← Entry point
│       ├── controller/
│       │   ├── AuthController.java        ← /api/auth/*
│       │   └── ReviewController.java      ← /api/reviews/*
│       ├── service/
│       │   ├── AuthService.java
│       │   ├── CodeReviewService.java     ← Core business logic
│       │   ├── CustomUserDetailsService.java
│       │   └── PdfReportService.java
│       ├── ai/
│       │   ├── GroqAiService.java         ← Groq API integration
│       │   └── AiResponseParser.java      ← Parse AI JSON response
│       ├── entity/
│       │   ├── User.java
│       │   └── CodeReview.java
│       ├── dto/
│       │   ├── AuthDto.java
│       │   └── ReviewDto.java
│       ├── repository/
│       │   ├── UserRepository.java
│       │   └── CodeReviewRepository.java
│       ├── security/
│       │   ├── JwtUtils.java
│       │   └── JwtAuthFilter.java
│       └── config/
│           ├── SecurityConfig.java
│           └── AppConfig.java
│
└── frontend/
    ├── package.json
    └── src/
        ├── App.js                         ← Routes
        ├── index.js
        ├── hooks/
        │   └── useAuth.js                 ← Auth context
        ├── utils/
        │   └── api.js                     ← Axios API client
        ├── components/
        │   ├── Layout.jsx                 ← Sidebar + nav
        │   └── ScoreCards.jsx
        ├── pages/
        │   ├── LoginPage.jsx
        │   ├── RegisterPage.jsx
        │   ├── DashboardPage.jsx
        │   ├── ReviewPage.jsx             ← Monaco editor + submit
        │   ├── ReviewDetailPage.jsx       ← Full review view
        │   └── HistoryPage.jsx
        └── styles/
            └── globals.css               ← Dark theme design system
```

---

## 🌐 API Reference

### Auth

| Method | Endpoint | Body | Description |
|---|---|---|---|
| POST | `/api/auth/register` | `{username, email, password}` | Register |
| POST | `/api/auth/login` | `{username, password}` | Login → JWT |

### Reviews (requires `Authorization: Bearer <token>`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/reviews` | Submit code for review |
| GET | `/api/reviews/history` | Paginated history |
| GET | `/api/reviews/:id` | Get review by ID |
| POST | `/api/reviews/:id/refactor` | AI refactor the code |
| GET | `/api/reviews/:id/pdf` | Download PDF report |
| DELETE | `/api/reviews/:id` | Delete review |
| GET | `/api/reviews/dashboard` | Stats & analytics |

---

## ☁️ Free Deployment Guide

### Backend → Render

1. Push to GitHub
2. Go to [render.com](https://render.com) → New Web Service
3. Connect your repo
4. Set environment variables:
   - `GROQ_API_KEY=your_key`
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://...` (from Neon)
   - `SPRING_DATASOURCE_USERNAME=...`
   - `SPRING_DATASOURCE_PASSWORD=...`
5. Build command: `mvn clean package -DskipTests`
6. Start command: `java -jar target/ai-code-review-1.0.0.jar`

### Database → Neon (Free PostgreSQL)

1. Go to [neon.tech](https://neon.tech)
2. Create project → copy connection string
3. Use in environment variables above

### Frontend → Vercel

1. `cd frontend && npm run build`
2. Push to GitHub
3. Import to [vercel.com](https://vercel.com)
4. Set env var: `REACT_APP_API_URL=https://your-backend.onrender.com/api`

---

## 🔄 Switch to PostgreSQL (Production)

In `application.properties`, comment H2 and uncomment PostgreSQL lines:

```properties
spring.datasource.url=jdbc:postgresql://your-host:5432/codereviewdb
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## 🧠 Prompt Engineering

The AI prompt is in `GroqAiService.java`. The system asks Llama 3 to return structured JSON:

```json
{
  "qualityScore": 75,
  "readabilityScore": 80,
  "securityScore": 60,
  "performanceScore": 70,
  "summary": "...",
  "issues": ["..."],
  "suggestions": ["..."],
  "securityWarnings": ["..."],
  "performanceNotes": ["..."]
}
```

You can tune the prompt to be stricter, more verbose, or domain-specific.

---

## 🎯 Resume Highlights

This project demonstrates:
- **Backend**: Spring Boot 3, Spring Security, JWT, JPA, RESTful API design
- **AI Integration**: Groq API, prompt engineering, structured JSON parsing
- **Frontend**: React 18, React Router, Monaco Editor (VS Code's editor), Recharts
- **Database**: JPA entities, repositories, relational design
- **Security**: JWT auth, BCrypt hashing, CORS, input validation
- **DevOps**: Deployable to Render (backend), Vercel (frontend), Neon (DB)
- **PDF Generation**: iText PDF library

---

## 📝 License

MIT — free to use, modify, and deploy.
