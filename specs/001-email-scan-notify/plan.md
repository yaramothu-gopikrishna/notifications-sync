# Implementation Plan: Email Scan & Mobile Notifications

**Branch**: `001-email-scan-notify` | **Date**: 2026-02-21 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-email-scan-notify/spec.md`

## Summary

Build a backend service that connects to users' Gmail accounts via OAuth2, periodically scans for new unread emails, and delivers notifications to Slack and/or WhatsApp. The system uses Java 21 with Spring Boot 3, follows SOLID principles, and employs circuit breaker and rate limiter patterns (via Resilience4j) for all external integrations. PostgreSQL for persistence, Redis for caching/rate-limiting state, and Docker for deployment.

## Technical Context

**Language/Version**: Java 21 (LTS)
**Framework**: Spring Boot 3.3+ with Spring Security, Spring Data JPA, Spring Scheduler
**Primary Dependencies**: Resilience4j (circuit breaker, rate limiter, retry, bulkhead), Gmail API Client, Slack API SDK, Twilio WhatsApp SDK, Flyway (migrations), MapStruct (DTO mapping), Lombok
**Storage**: PostgreSQL 16 (primary), Redis 7 (caching, rate limiter state, circuit breaker state, deduplication)
**Testing**: JUnit 5, Mockito, Testcontainers (PostgreSQL, Redis), WireMock (external API mocking), ArchUnit (architecture rules)
**Target Platform**: Docker containers (Linux)
**Project Type**: Single backend service (API + scheduler)
**Performance Goals**: Process 1,000 concurrent mailboxes, deliver 95% of notifications within 2 minutes, handle burst of 100+ emails per user
**Constraints**: <500ms p95 API response, respect Gmail/Slack/WhatsApp rate limits, OAuth2 tokens never stored in plaintext
**Scale/Scope**: 1,000+ concurrent users, ~5 API endpoints, 3 external integrations (Gmail, Slack, WhatsApp)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Constitution is template-only (no project-specific principles ratified yet). No gate violations. Proceeding with industry best practices:
- **SOLID Principles**: Enforced — interfaces for all service contracts, single-responsibility per class, dependency injection throughout
- **Circuit Breaker**: Applied to all external API calls (Gmail, Slack, WhatsApp) via Resilience4j
- **Rate Limiter**: Applied to outbound API calls and inbound user API endpoints via Resilience4j
- **Error Handling**: Centralized exception handling with structured error responses, retry with exponential backoff for transient failures
- **Scalability**: Stateless service design, Redis-backed distributed state, horizontally scalable

## Project Structure

### Documentation (this feature)

```text
specs/001-email-scan-notify/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (OpenAPI specs)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/notifications/
│   │   ├── NotificationsApplication.java
│   │   ├── config/                    # Spring configuration, Resilience4j, Redis, Security
│   │   │   ├── ResilienceConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── SchedulerConfig.java
│   │   ├── domain/                    # Domain entities (JPA)
│   │   │   ├── User.java
│   │   │   ├── EmailAccount.java
│   │   │   ├── NotificationChannel.java
│   │   │   ├── Notification.java
│   │   │   └── FilterRule.java
│   │   ├── repository/                # Spring Data JPA repositories
│   │   │   ├── UserRepository.java
│   │   │   ├── EmailAccountRepository.java
│   │   │   ├── NotificationChannelRepository.java
│   │   │   ├── NotificationRepository.java
│   │   │   └── FilterRuleRepository.java
│   │   ├── service/                   # Business logic (interfaces + implementations)
│   │   │   ├── auth/
│   │   │   │   ├── AuthService.java
│   │   │   │   └── AuthServiceImpl.java
│   │   │   ├── email/
│   │   │   │   ├── EmailScannerService.java
│   │   │   │   ├── EmailScannerServiceImpl.java
│   │   │   │   └── GmailClientService.java
│   │   │   ├── notification/
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── NotificationServiceImpl.java
│   │   │   │   ├── NotificationDispatcher.java
│   │   │   │   ├── SlackNotificationSender.java
│   │   │   │   ├── WhatsAppNotificationSender.java
│   │   │   │   └── NotificationSender.java        # Interface (Strategy pattern)
│   │   │   ├── filter/
│   │   │   │   ├── FilterService.java
│   │   │   │   └── FilterServiceImpl.java
│   │   │   └── dedup/
│   │   │       ├── DeduplicationService.java
│   │   │       └── RedisDeduplicationServiceImpl.java
│   │   ├── controller/                # REST API controllers
│   │   │   ├── AuthController.java
│   │   │   ├── EmailAccountController.java
│   │   │   ├── NotificationChannelController.java
│   │   │   ├── FilterRuleController.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── dto/                       # Request/Response DTOs
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── mapper/                    # MapStruct mappers
│   │   ├── scheduler/                 # Scheduled tasks (email scanning)
│   │   │   └── EmailScanScheduler.java
│   │   └── exception/                 # Custom exceptions
│   │       ├── EmailConnectionException.java
│   │       ├── NotificationDeliveryException.java
│   │       └── RateLimitExceededException.java
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/              # Flyway migrations
│           ├── V1__create_users.sql
│           ├── V2__create_email_accounts.sql
│           ├── V3__create_notification_channels.sql
│           ├── V4__create_notifications.sql
│           └── V5__create_filter_rules.sql
└── test/
    ├── java/com/notifications/
    │   ├── unit/                      # Unit tests (mocked dependencies)
    │   │   ├── service/
    │   │   └── controller/
    │   ├── integration/               # Integration tests (Testcontainers)
    │   │   ├── repository/
    │   │   ├── service/
    │   │   └── controller/
    │   └── architecture/              # ArchUnit tests (SOLID enforcement)
    │       └── ArchitectureTest.java
    └── resources/
        └── application-test.yml

docker-compose.yml                     # PostgreSQL, Redis, app
Dockerfile
pom.xml
```

**Structure Decision**: Single Spring Boot backend service. No frontend — users interact through API endpoints for setup, and receive notifications passively. The service combines REST API (for user management/settings) with a scheduler (for email polling). This keeps deployment simple while maintaining clear separation of concerns via package structure following SOLID principles.

## Complexity Tracking

No constitution violations to justify. Architecture follows standard Spring Boot patterns with Resilience4j for resilience.
