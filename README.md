# PeerForge

PeerForge is a mentoring marketplace for software engineers and people learning technology. Learners can discover mentors, request sessions, message each other, pay for a session, and leave a review after completion. Mentors manage their profile and availability; administrators review mentor applications and moderate reviews.

This repository contains the Spring Boot REST API. The React frontend is maintained separately in the [`peerforge-frontend`](https://github.com/vdushyant/peerforge-frontend) repository.

## Project repositories

| Component | Stack |
| --- | --- |
| Backend API (this repository) | Java 21, Spring Boot, Spring Security, JPA, PostgreSQL |
| Frontend | React, TypeScript, Vite |
| Database | PostgreSQL |
| Cache | Redis |

## Main capabilities

- Email/password accounts and JWT access/refresh tokens.
- User profiles and skills.
- Mentor applications, approval/rejection by administrators, mentor profiles, and recurring availability.
- Session booking, confirmation, cancellation, completion, meeting links, and session chat.
- Razorpay UPI QR payment generation and signed webhook processing.
- Session reviews, mentor ratings, and moderation.
- In-app notifications and role-specific dashboards.
- Redis caching for selected public responses is disabled by default; see [Redis caching](docs/redis-caching.md).

## Prerequisites

- Java 21
- PostgreSQL
- Git
- Node.js and npm for the separate frontend
- Docker to run the local Redis container

## Run the backend locally

1. Create a PostgreSQL database named `peerforge` and a database user with access to it.
2. Start the Spring Boot application from IntelliJ, or run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. The API listens on `http://localhost:8080`. Flyway applies schema migrations on startup. OpenAPI documentation is available at `http://localhost:8080/swagger-ui/index.html`.

## Run the frontend

In the frontend repository:

```powershell
npm install
$env:VITE_API_BASE_URL = "http://localhost:8080/api/v1"
npm run dev
```

Vite prints the local frontend URL. For a production build, set `VITE_API_BASE_URL` to the deployed API base URL and run `npm run build`; the built static files are written to `dist/`.

## API groups

The API is organized around `/api/v1`:

- `/auth` — register, login, refresh, and logout
- `/public`, `/skills`, `/mentors` — public discovery and mentor availability/profile
- `/profile`, `/sessions`, `/notifications` — authenticated learner/mentor activity
- `/sessions/{id}/reviews` and `/mentors/{id}/reviews` — session review eligibility and mentor reviews
- `/payments` and `/webhooks/razorpay` — checkout order, verification, payment status, and webhook processing
- `/admin` — administrator views and mentor/review moderation
- `/dashboard` — mentor and platform summaries

For the full route list and request models, consult Swagger UI while the backend is running or the controllers under `src/main/java/com/peerforge`.

## Related docs

- [Redis caching guide](docs/redis-caching.md)
- [Frontend repository](https://github.com/vdushyant/peerforge-frontend)
