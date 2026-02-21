# Email Scan & Mobile Notifications — Developer Quickstart

## Prerequisites

| Tool / Account | Version | Notes |
|---|---|---|
| **Java** | 21 (LTS) | `java -version` to verify |
| **Docker & Docker Compose** | Latest stable | Required for local PostgreSQL + Redis |
| **Maven** | 3.9+ (wrapper included) | `./mvnw` ships with the repo |
| **Google Cloud** | — | Project with **Gmail API** enabled + OAuth2 credentials |
| **Slack** | — | Workspace with a **Slack App** and Bot token |
| **Twilio** | — | Account with a **WhatsApp-enabled** phone number |

---

## Local Setup

### 1. Clone & checkout

```bash
git clone <repo-url> && cd notifications
git checkout 001-email-scan-notify
```

### 2. Start infrastructure

```bash
docker-compose up -d   # PostgreSQL 16 + Redis 7
```

Verify the containers are healthy:

```bash
docker-compose ps
```

### 3. Configure credentials

```bash
cp application-dev.yml.example application-dev.yml
```

Open `application-dev.yml` and fill in the values described in [Environment Variables](#environment-variables) below.

### 4. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API starts on `http://localhost:8080` by default.

---

## Environment Variables

Set these in `application-dev.yml` or export them in your shell.

### Encryption

| Variable | Description |
|---|---|
| `ENCRYPTION_KEY` | AES-256 key for OAuth2 token encryption (base64-encoded, 32 bytes) |

Generate a key:

```bash
openssl rand -base64 32
```

### Google / Gmail OAuth2

| Variable | Description |
|---|---|
| `GOOGLE_CLIENT_ID` | OAuth2 client ID from Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | OAuth2 client secret |
| `GOOGLE_PUBSUB_PROJECT_ID` | GCP project ID for Cloud Pub/Sub |
| `GOOGLE_PUBSUB_TOPIC` | Pub/Sub topic for Gmail push notifications |
| `GOOGLE_PUBSUB_SUBSCRIPTION` | Pub/Sub subscription name |

### Slack

| Variable | Description |
|---|---|
| `SLACK_BOT_TOKEN` | Bot token (`xoxb-…`) for testing; production stores per-user tokens |

### Twilio (WhatsApp)

| Variable | Description |
|---|---|
| `TWILIO_ACCOUNT_SID` | Twilio Account SID |
| `TWILIO_AUTH_TOKEN` | Twilio Auth Token |
| `TWILIO_WHATSAPP_FROM` | WhatsApp-enabled sender number (e.g. `whatsapp:+14155238886`) |

### Database (PostgreSQL)

| Variable | Description |
|---|---|
| `DB_URL` | JDBC connection URL (e.g. `jdbc:postgresql://localhost:5432/notifications`) |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |

### Redis

| Variable | Description |
|---|---|
| `REDIS_HOST` | Redis host (default `localhost`) |
| `REDIS_PORT` | Redis port (default `6379`) |

### Auth

| Variable | Description |
|---|---|
| `JWT_SECRET` | JWT signing key for API authentication |

---

## Running Tests

**Unit tests:**

```bash
./mvnw test
```

**Integration tests** (uses Testcontainers — Docker must be running):

```bash
./mvnw verify -P integration-test
```

---

## Docker Build

Build the application image:

```bash
docker build -t email-scan-notify .
```

Run the full stack (app + PostgreSQL + Redis):

```bash
docker-compose up
```

---

## API Testing

> Replace `localhost:8080` with your host if different.

### Register a user

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "dev@example.com", "password": "SecurePass123!"}' | jq
```

### Login

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "dev@example.com", "password": "SecurePass123!"}' | jq
```

Save the returned token:

```bash
export TOKEN=<token-from-response>
```

### Connect an email account (Gmail OAuth2 flow)

```bash
curl -s -X POST http://localhost:8080/api/v1/email-accounts/connect/google \
  -H "Authorization: Bearer $TOKEN" | jq
```

Follow the returned `authorizationUrl` to complete the OAuth2 consent flow.

### Add a notification channel

```bash
curl -s -X POST http://localhost:8080/api/v1/channels \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type": "SLACK", "config": {"webhookUrl": "https://hooks.slack.com/services/T.../B.../xxx"}}' | jq
```

---

## Troubleshooting

| Problem | Fix |
|---|---|
| **Port 5432 / 6379 already in use** | Stop local PostgreSQL/Redis (`brew services stop postgresql redis`) or change ports in `docker-compose.yml`. |
| **Missing environment variables** | The app fails fast with a clear message. Double-check `application-dev.yml` has all required values. |
| **OAuth2 redirect mismatch** | In Google Cloud Console, add `http://localhost:8080/api/v1/auth/oauth2/callback/google` to **Authorized redirect URIs**. |
| **Testcontainers can't start** | Ensure Docker is running and your user has permission (`docker ps`). |
| **JWT errors on API calls** | Confirm `JWT_SECRET` is set and use a fresh token from `/auth/login`. |
| **Twilio WhatsApp not sending** | Verify the sender number is WhatsApp-enabled and the recipient has opted in via the Twilio sandbox. |
