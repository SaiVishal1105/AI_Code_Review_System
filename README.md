<div align="center">

<img src="https://img.shields.io/badge/AI-Powered-6366f1?style=for-the-badge&logo=openai&logoColor=white" />
<img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
<img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black" />
<img src="https://img.shields.io/badge/Groq-Llama_3.3-f59e0b?style=for-the-badge" />
<img src="https://img.shields.io/badge/PostgreSQL-Neon-00E599?style=for-the-badge&logo=postgresql&logoColor=white" />

<br/><br/>

# 🤖 AI Code Review System

### An intelligent code review platform powered by Groq's Llama 3.3 — built with Spring Boot & React

[Live Demo](#) · [Report Bug](../../issues) · [Request Feature](../../issues)

<br/>

![App Screenshot](https://placehold.co/900x500/0d1526/6366f1?text=AI+Code+Review+System&font=montserrat)

</div>

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Deployment](#-deployment)
- [Project Structure](#-project-structure)
- [Screenshots](#-screenshots)

---

## 🧠 About

**AI Code Review System** is a full-stack web application that uses large language models to review your code in seconds. Paste any code snippet, select the language, and get back a structured review with bug detection, security warnings, performance insights, and a quality score — all powered by **Groq's Llama 3.3 70B** model running at blazing speed.

Built as a portfolio project showcasing real-world integration of AI APIs, Spring Boot backend design, JWT authentication, and a modern React frontend.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔍 **AI Code Review** | Detects bugs, security issues, and anti-patterns using Llama 3.3 70B |
| 📊 **Score Dashboard** | Quality, Readability, Security, and Performance scores (0–100) |
| ✨ **AI Refactor** | One-click AI rewrite of your code following best practices |
| 📄 **PDF Reports** | Download a formatted PDF of any review |
| 🕒 **Review History** | All reviews persisted in PostgreSQL with full pagination |
| 📈 **Analytics** | Dashboard with charts showing review trends by language |
| 🔐 **JWT Auth** | Secure register/login with BCrypt password hashing |
| 🖊️ **Monaco Editor** | VS Code's editor embedded in the browser with syntax highlighting |
| 🌐 **Multi-language** | Java, Python, JavaScript, TypeScript, C++, Go, Rust, Kotlin |

---

## 🛠️ Tech Stack

**Backend**
- Java 17 + Spring Boot 3.2
- Spring Security + JWT (jjwt 0.12)
- Spring Data JPA + Hibernate
- PostgreSQL (Neon) / H2 (dev)
- iText 5 (PDF generation)
- Java HttpClient (Groq API calls)

**Frontend**
- React 18 + React Router 6
- Monaco Editor (`@monaco-editor/react`)
- Recharts (analytics dashboard)
- Axios + React Hot Toast
- Custom dark theme (CSS variables)

**AI & Infra**
- Groq API — Llama 3.3 70B Versatile (free tier)
- Neon PostgreSQL (free tier)
- Render (backend hosting — free tier)
- Vercel (frontend hosting — free tier)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                  React Frontend                      │
│         Monaco Editor · Recharts · Axios            │
└───────────────────────┬─────────────────────────────┘
                        │ HTTPS / REST
┌───────────────────────▼─────────────────────────────┐
│              Spring Boot Backend                     │
│                                                      │
│  AuthController    ReviewController                  │
│       │                  │                           │
│  AuthService       CodeReviewService                 │
│       │                  │                           │
│  JWT + BCrypt      GroqAiService ──────────────────► Groq API
│                          │                           │  (Llama 3.3)
│                    PdfReportService                  │
│                          │                           │
└───────────────────────┬──┼──────────────────────────┘
                        │  │
┌───────────────────────▼──▼──────────────────────────┐
│           Neon PostgreSQL Database                   │
│              users · code_reviews                    │
└─────────────────────────────────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Maven 3.8+
- Free [Groq API key](https://console.groq.com) — generous free tier, no credit card

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/ai-code-review.git
cd ai-code-review
```

### 2. Configure Backend

```bash
cd backend
```

Open `src/main/resources/application.properties` and set your Groq key:

```properties
groq.api.key=gsk_your_key_here
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.model=llama-3.3-70b-versatile
```

### 3. Run Backend

```bash
mvn spring-boot:run
```

Backend starts at **http://localhost:8080**
H2 Console (dev): **http://localhost:8080/h2-console**

### 4. Run Frontend

```bash
cd ../frontend
npm install
npm start
```

Frontend starts at **http://localhost:3000**

### 5. Register & Start Reviewing

Open **http://localhost:3000**, create an account, and submit your first code review!

---

## 🔑 Getting a Free Groq API Key

1. Visit **[console.groq.com](https://console.groq.com)**
2. Sign up — no credit card required
3. Go to **API Keys** → **Create API Key**
4. Copy the key (starts with `gsk_`) and paste into `application.properties`

**Free tier limits:** ~14,400 requests/day with Llama 3.3 70B — more than enough for development and demos.

---

## 📡 API Reference

All protected endpoints require `Authorization: Bearer <token>` header.

### Auth

| Method | Endpoint | Body | Response |
|---|---|---|---|
| `POST` | `/api/auth/register` | `{username, email, password}` | `{token, username, email}` |
| `POST` | `/api/auth/login` | `{username, password}` | `{token, username, email}` |

### Reviews

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/reviews` | Submit code for AI review |
| `GET` | `/api/reviews/history` | Paginated review history |
| `GET` | `/api/reviews/:id` | Get a specific review |
| `POST` | `/api/reviews/:id/refactor` | AI refactor the code |
| `GET` | `/api/reviews/:id/pdf` | Download PDF report |
| `DELETE` | `/api/reviews/:id` | Delete a review |
| `GET` | `/api/reviews/dashboard` | Stats and analytics |

### Example Request & Response

```bash
POST /api/reviews
Authorization: Bearer eyJhbGci...

{
  "language": "java",
  "code": "public class Calculator { public int divide(int a, int b) { return a/b; } }"
}
```

```json
{
  "id": 42,
  "language": "java",
  "qualityScore": 55,
  "readabilityScore": 70,
  "securityScore": 60,
  "performanceScore": 80,
  "summary": "The code is functional but lacks input validation and error handling.",
  "issues": [
    "Division by zero not handled — will throw ArithmeticException at runtime",
    "No input validation on parameters"
  ],
  "suggestions": [
    "Add a check for b == 0 and throw IllegalArgumentException",
    "Consider returning Optional<Integer> for safer API design"
  ],
  "securityWarnings": [],
  "performanceNotes": ["Method is O(1) — optimal for this operation"]
}
```

---

## ☁️ Deployment (100% Free)

### Database → Neon PostgreSQL

1. Go to **[neon.tech](https://neon.tech)** → Create project
2. Copy the JDBC connection string
3. Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Backend → Render

1. Push to GitHub
2. Go to **[render.com](https://render.com)** → New Web Service → Connect repo
3. Build command: `mvn clean package -DskipTests`
4. Start command: `java -jar target/ai-code-review-1.0.0.jar`
5. Add environment variables:
   - `GROQ_API_KEY`
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`

### Frontend → Vercel

1. `cd frontend && npm run build`
2. Push to GitHub
3. Import to **[vercel.com](https://vercel.com)**
4. Set env var: `REACT_APP_API_URL=https://your-backend.onrender.com/api`

---

## 📁 Project Structure

```
ai-code-review/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/codereview/
│       ├── AiCodeReviewApplication.java
│       ├── ai/
│       │   ├── GroqAiService.java          # Groq API integration
│       │   └── AiResponseParser.java       # Parse AI JSON response
│       ├── controller/
│       │   ├── AuthController.java         # /api/auth/*
│       │   └── ReviewController.java       # /api/reviews/*
│       ├── service/
│       │   ├── AuthService.java
│       │   ├── CodeReviewService.java      # Core business logic
│       │   └── PdfReportService.java       # PDF generation
│       ├── entity/
│       │   ├── User.java
│       │   └── CodeReview.java
│       ├── security/
│       │   ├── JwtUtils.java
│       │   └── JwtAuthFilter.java
│       └── config/
│           └── SecurityConfig.java
│
└── frontend/
    └── src/
        ├── App.js                          # Routes
        ├── hooks/useAuth.js                # Auth context
        ├── utils/api.js                    # Axios client
        ├── components/
        │   ├── Layout.jsx                  # Sidebar navigation
        │   └── ScoreCards.jsx              # Score display
        └── pages/
            ├── LoginPage.jsx
            ├── RegisterPage.jsx
            ├── DashboardPage.jsx           # Analytics + charts
            ├── ReviewPage.jsx              # Monaco editor
            ├── ReviewDetailPage.jsx        # Full review view
            └── HistoryPage.jsx             # Review history table
```

---

## 🎯 What This Project Demonstrates

This project was built to showcase production-level full-stack skills:

- **REST API design** — clean endpoints, proper HTTP status codes, global exception handling
- **Spring Security** — stateless JWT auth, BCrypt hashing, CORS configuration
- **AI integration** — prompt engineering, structured JSON parsing, fallback handling
- **JPA & database design** — entity relationships, custom queries, pagination
- **React architecture** — context API, protected routes, custom hooks, Axios interceptors
- **PDF generation** — iText library with custom styled reports
- **Cloud deployment** — Render + Vercel + Neon free tier stack

---

## 📄 License

MIT License — free to use, fork, and build on.

---

<div align="center">

Built with ❤️ using Spring Boot, React, and Groq AI

⭐ Star this repo if you found it helpful!

</div>
