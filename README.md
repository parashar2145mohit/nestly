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

## Deploy the complete stack on Render

The root `render.yaml` is a Render Blueprint for the complete Nestly stack:

- `nestly-web`: public React/Nginx web service
- `nestly-backend`: public Spring Boot API
- `nestly-ai`: FastAPI AI web service (used over Render's private network)
- `nestly-mysql`: private MySQL 8 service with a 10 GB persistent disk

Deployment steps:

1. Push this repository to GitHub.
2. Open the Render Dashboard and choose **New → Blueprint**.
3. Connect the GitHub repository and select `render.yaml`.
4. Review the resources and provide any optional secret values Render requests. Razorpay and LLM keys can be left blank for the local-demo feature set.
5. Apply the Blueprint and wait for MySQL, AI, backend, and web deployments to become healthy.
6. Open the `nestly-web` URL shown by Render.

Render automatically generates the MySQL passwords and JWT secret. They are never committed to the repository. All services are configured in the Singapore region. MySQL uses a persistent disk and therefore requires a paid Render resource; review Render's estimated monthly cost before applying the Blueprint.

For production, change the seeded demo passwords or remove `DemoDataConfig`, configure real payment/LLM keys, and restrict public access to Swagger and the AI service if they are not meant to be exposed.
