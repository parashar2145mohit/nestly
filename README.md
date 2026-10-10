# Nestly

Nestly is a multi-tenant residential society management platform. The repository contains a Spring Boot API, React web app, Python AI service, and a MySQL database orchestrated with Docker Compose.

## Quick start

```bash
cp .env.example .env
docker compose up --build
```

- Web app: http://localhost:3000
- API and Swagger: http://localhost:8080/swagger-ui.html
- AI service docs: http://localhost:8000/docs

## Demo accounts

The local demo seeds Oakwood Residency and three role-based accounts automatically:

| Role | Email | Password |
| --- | --- | --- |
| Society Admin | `admin@nestly.local` | `Nestly@123` |
| Resident | `resident@nestly.local` | `Nestly@123` |
| Security Guard | `guard@nestly.local` | `Nestly@123` |

Use the **Sign in** button on the landing page. Each account opens a role-appropriate workspace. Demo interactions such as complaints, visitor passes, payments, bookings, and status updates are available immediately.

## Architecture

```text
React 18 ──JWT/REST──> Spring Boot 3 ──REST──> FastAPI / scikit-learn
                             │
                          MySQL 8
```

Every tenant-owned record carries a society identifier. The authenticated user's society claim is used by the API to scope data access.

## Local development

The containerized path is recommended. For direct local development, use Java 17+, Node 20+, Python 3.11+, and MySQL 8.

```bash
cd frontend && npm install && npm run dev
cd ai-service && pip install -r requirements.txt && uvicorn app.main:app --reload
cd backend && ./mvnw spring-boot:run
```

## Environment

See `.env.example`. Change all secrets outside development.
