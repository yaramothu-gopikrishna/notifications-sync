# Tasks: Email Scan & Mobile Notifications

**Input**: Design documents from `/specs/001-email-scan-notify/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/api.yaml ✅

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[USx]**: Which user story this task belongs to (omitted for setup/foundational tasks)
- All source paths relative to repo root

## Path Conventions

- **Source**: `src/main/java/com/notifications/`
- **Resources**: `src/main/resources/`
- **Migrations**: `src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, dependency management, and baseline configuration

- [ ] T001 Initialize Spring Boot 3.3+ project with Java 21 — generate `pom.xml` with dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, spring-boot-starter-actuator, postgresql driver, flyway-core, lombok, mapstruct + mapstruct-processor, resilience4j-spring-boot3, spring-boot-starter-data-redis, google-api-services-gmail, google-auth-library-oauth2-http, com.slack.api:slack-api-client, com.twilio.sdk:twilio, jjwt-api/impl/jackson, spring-cloud-gcp-starter-pubsub
- [ ] T002 [P] Create main application class `src/main/java/com/notifications/NotificationsApplication.java` with `@SpringBootApplication` annotation
- [ ] T003 [P] Create base `application.yml` at `src/main/resources/application.yml` — configure server port 8080, datasource (PostgreSQL 16), JPA hibernate ddl-auto=validate, flyway enabled, Redis host/port, logging levels, and placeholder sections for gmail/slack/whatsapp properties
- [ ] T004 [P] Create `application-dev.yml` at `src/main/resources/application-dev.yml` — local PostgreSQL (localhost:5432/notifications_dev), local Redis (localhost:6379), debug logging, Gmail/Slack/WhatsApp sandbox credentials placeholders
- [ ] T005 [P] Create `application-prod.yml` at `src/main/resources/application-prod.yml` — environment variable references for all secrets (`${DB_URL}`, `${REDIS_URL}`, `${GMAIL_CLIENT_SECRET}`, etc.), info logging level
- [ ] T006 [P] Create `Dockerfile` at repo root — multi-stage build with Eclipse Temurin 21-jdk for build, 21-jre for runtime, expose port 8080, non-root user
- [ ] T007 [P] Create `docker-compose.yml` at repo root — services for PostgreSQL 16 (port 5432, volume mount), Redis 7 (port 6379), and the Spring Boot application (depends_on postgres/redis, env_file)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Database Migrations

- [ ] T008 Create Flyway migration `src/main/resources/db/migration/V1__create_users.sql` — CREATE TABLE users with columns: id (UUID PK), email (VARCHAR 320 UNIQUE NOT NULL), password_hash (VARCHAR 72 NOT NULL), notifications_paused (BOOLEAN DEFAULT FALSE), created_at/updated_at (TIMESTAMPTZ) per data-model.md DDL
- [ ] T009 [P] Create Flyway migration `src/main/resources/db/migration/V2__create_email_accounts.sql` — CREATE TABLE email_accounts with all columns per data-model.md DDL including status CHECK constraint, encrypted token columns, history_id, token_expires_at, UNIQUE(user_id, email_address), and all indexes (ix_email_accounts_user_id, ix_email_accounts_status, ix_email_accounts_token_expires)
- [ ] T010 [P] Create Flyway migration `src/main/resources/db/migration/V3__create_notification_channels.sql` — CREATE TABLE notification_channels with all columns per data-model.md DDL including channel_type CHECK, status CHECK, UNIQUE(user_id, channel_type), and all indexes
- [ ] T011 [P] Create Flyway migration `src/main/resources/db/migration/V4__create_notifications.sql` — CREATE TABLE notifications with all columns per data-model.md DDL including delivery_status CHECK, all FK constraints (ON DELETE CASCADE), and all 5 indexes
- [ ] T012 [P] Create Flyway migration `src/main/resources/db/migration/V5__create_filter_rules.sql` — CREATE TABLE filter_rules with all columns per data-model.md DDL including rule_type CHECK, and indexes (ix_filter_rules_user_active, ix_filter_rules_user_type)

### Domain Entities

- [ ] T013 Create User entity at `src/main/java/com/notifications/domain/User.java` — JPA `@Entity` mapped to `users` table with UUID id (`@GeneratedValue`), email, passwordHash, notificationsPaused, createdAt/updatedAt (`@CreationTimestamp`/`@UpdateTimestamp`), Lombok `@Getter @Setter @NoArgsConstructor`
- [ ] T014 [P] Create EmailAccount entity at `src/main/java/com/notifications/domain/EmailAccount.java` — JPA entity mapped to `email_accounts`, `@ManyToOne` to User, status field as String with valid values (active/paused/error), encrypted token fields (plain String — encryption handled by JPA converter), historyId, tokenExpiresAt, lastScannedAt
- [ ] T015 [P] Create NotificationChannel entity at `src/main/java/com/notifications/domain/NotificationChannel.java` — JPA entity mapped to `notification_channels`, `@ManyToOne` to User, channelType (slack/whatsapp), status, botTokenEncrypted, slackChannelId, whatsappPhoneNumber, twilioSid, keyVersion, consentGiven, consentGivenAt
- [ ] T016 [P] Create Notification entity at `src/main/java/com/notifications/domain/Notification.java` — JPA entity mapped to `notifications`, `@ManyToOne` to User/EmailAccount/NotificationChannel, senderName, senderAddress, subject, preview(150), deliveryStatus, externalMessageId, retryCount, emailReceivedAt, deliveredAt
- [ ] T017 [P] Create FilterRule entity at `src/main/java/com/notifications/domain/FilterRule.java` — JPA entity mapped to `filter_rules`, `@ManyToOne` to User, ruleType (sender/subject), pattern, isActive, priority

### Repositories

- [ ] T018 Create UserRepository at `src/main/java/com/notifications/repository/UserRepository.java` — extends `JpaRepository<User, UUID>`, add `Optional<User> findByEmail(String email)`, `boolean existsByEmail(String email)`
- [ ] T019 [P] Create EmailAccountRepository at `src/main/java/com/notifications/repository/EmailAccountRepository.java` — extends `JpaRepository<EmailAccount, UUID>`, add `List<EmailAccount> findByUserIdAndStatus(UUID userId, String status)`, `List<EmailAccount> findByStatus(String status)`, `Optional<EmailAccount> findByIdAndUserId(UUID id, UUID userId)`, `List<EmailAccount> findByUserId(UUID userId)`
- [ ] T020 [P] Create NotificationChannelRepository at `src/main/java/com/notifications/repository/NotificationChannelRepository.java` — extends `JpaRepository<NotificationChannel, UUID>`, add `List<NotificationChannel> findByUserIdAndStatus(UUID userId, String status)`, `Optional<NotificationChannel> findByIdAndUserId(UUID id, UUID userId)`, `List<NotificationChannel> findByUserId(UUID userId)`
- [ ] T021 [P] Create NotificationRepository at `src/main/java/com/notifications/repository/NotificationRepository.java` — extends `JpaRepository<Notification, UUID>`, add `Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable)`, `List<Notification> findByDeliveryStatus(String status)`
- [ ] T022 [P] Create FilterRuleRepository at `src/main/java/com/notifications/repository/FilterRuleRepository.java` — extends `JpaRepository<FilterRule, UUID>`, add `List<FilterRule> findByUserIdAndIsActive(UUID userId, boolean isActive)`, `Optional<FilterRule> findByIdAndUserId(UUID id, UUID userId)`, `List<FilterRule> findByUserId(UUID userId)`

### Security & Auth Infrastructure

- [ ] T023 Create SecurityConfig at `src/main/java/com/notifications/config/SecurityConfig.java` — `@Configuration @EnableWebSecurity`, configure `SecurityFilterChain`: permit `/api/v1/auth/**` and `/api/v1/email-accounts/callback` unauthenticated, require authentication for all other endpoints, stateless session management, add JWT authentication filter, disable CSRF
- [ ] T024 [P] Implement JWT utility class at `src/main/java/com/notifications/config/JwtTokenProvider.java` — generate access tokens (15 min expiry) and refresh tokens (7 day expiry) using io.jsonwebtoken (jjwt), extract userId from token, validate token signature (HS256 with configurable secret from `application.yml`), throw custom exceptions on expired/invalid tokens
- [ ] T025 Implement JWT authentication filter at `src/main/java/com/notifications/config/JwtAuthenticationFilter.java` — extends `OncePerRequestFilter`, extract Bearer token from Authorization header, validate via JwtTokenProvider, set `UsernamePasswordAuthenticationToken` in SecurityContext with userId as principal

### Cross-Cutting Infrastructure

- [ ] T026 [P] Create RedisConfig at `src/main/java/com/notifications/config/RedisConfig.java` — `@Configuration`, configure `RedisTemplate<String, String>` with `StringRedisSerializer`, configure `RedisConnectionFactory` from application properties
- [ ] T027 [P] Create ResilienceConfig at `src/main/java/com/notifications/config/ResilienceConfig.java` — `@Configuration`, define circuit breaker instances for gmail/slack/whatsapp using `CircuitBreakerRegistry` with count-based sliding window (size=20, failure threshold=50%, slow call threshold=80%/5s, wait-in-open=30s), define rate limiter instances per provider per research.md §4-5 configuration
- [ ] T028 [P] Create AES-256-GCM encryption converter at `src/main/java/com/notifications/config/EncryptionConverter.java` — implements `AttributeConverter<String, String>`, encrypt with AES/GCM/NoPadding (256-bit key from env var `ENCRYPTION_KEY`), output `base64(IV ‖ ciphertext ‖ authTag)`, decrypt by parsing base64 → extract IV (12 bytes) → decrypt; apply to token fields on EmailAccount and NotificationChannel via `@Convert`
- [ ] T029 [P] Create custom exception classes: `src/main/java/com/notifications/exception/EmailConnectionException.java` (extends RuntimeException), `src/main/java/com/notifications/exception/NotificationDeliveryException.java`, `src/main/java/com/notifications/exception/RateLimitExceededException.java`, `src/main/java/com/notifications/exception/ResourceNotFoundException.java`, `src/main/java/com/notifications/exception/TokenExpiredException.java`
- [ ] T030 [P] Create GlobalExceptionHandler at `src/main/java/com/notifications/controller/GlobalExceptionHandler.java` — `@RestControllerAdvice`, handle: `ResourceNotFoundException` → 404, `RateLimitExceededException` → 429 with Retry-After header, `EmailConnectionException` → 502, `NotificationDeliveryException` → 502, `MethodArgumentNotValidException` → 400 with field errors, `AuthenticationException` → 401, generic `Exception` → 500; return structured JSON `{ error, message, timestamp, path }`
- [ ] T031 [P] Create SchedulerConfig at `src/main/java/com/notifications/config/SchedulerConfig.java` — `@Configuration @EnableScheduling`, configure `ThreadPoolTaskScheduler` with pool size 5 for concurrent email scanning tasks

### DTOs & Mappers

- [ ] T032 Create request/response DTOs at `src/main/java/com/notifications/dto/request/` and `src/main/java/com/notifications/dto/response/` — RegisterRequest (email, password with @NotBlank @Email @Size), LoginRequest (email, password), RefreshTokenRequest (refreshToken), AuthTokenResponse (accessToken, refreshToken, expiresIn), EmailAccountResponse (id, provider, emailAddress, status, lastScannedAt, createdAt), NotificationChannelRequest (channelType, botToken, slackChannelId, whatsappPhoneNumber, twilioSid, consentGiven), NotificationChannelResponse, FilterRuleRequest (ruleType, pattern, isActive, priority), FilterRuleResponse, NotificationResponse (id, senderName, senderAddress, subject, preview, deliveryStatus, emailReceivedAt, deliveredAt, channelType), PaginatedResponse wrapper
- [ ] T033 [P] Create MapStruct mappers at `src/main/java/com/notifications/mapper/` — EmailAccountMapper (entity↔response), NotificationChannelMapper (request↔entity, entity↔response), FilterRuleMapper (request↔entity, entity↔response), NotificationMapper (entity↔response); all annotated with `@Mapper(componentModel = "spring")`

### Auth Service

- [ ] T034 Create AuthService interface at `src/main/java/com/notifications/service/auth/AuthService.java` — define methods: `AuthTokenResponse register(RegisterRequest request)`, `AuthTokenResponse login(LoginRequest request)`, `AuthTokenResponse refreshToken(String refreshToken)`
- [ ] T035 Implement AuthServiceImpl at `src/main/java/com/notifications/service/auth/AuthServiceImpl.java` — `@Service`, inject UserRepository + JwtTokenProvider + BCryptPasswordEncoder; register: validate email uniqueness → hash password → save User → generate tokens; login: find by email → verify password → generate tokens; refreshToken: validate refresh token → extract userId → generate new token pair
- [ ] T036 Create AuthController at `src/main/java/com/notifications/controller/AuthController.java` — `@RestController @RequestMapping("/api/v1/auth")`, POST `/register` → 201, POST `/login` → 200, POST `/refresh` → 200; delegate to AuthService, all three endpoints are unauthenticated per SecurityConfig

**Checkpoint**: Foundation ready — all entities, repositories, auth, config, and error handling in place. User story implementation can now begin.

---

## Phase 3: User Story 1 — Connect Email Account (Priority: P1) 🎯 MVP

**Goal**: Users can connect their Gmail account via OAuth2, view connection status, and disconnect accounts

**Independent Test**: Connect a Gmail account via OAuth2 flow, verify the account appears in GET /email-accounts with status "active"

### Implementation for User Story 1

- [ ] T037 [US1] Create GmailClientService at `src/main/java/com/notifications/service/email/GmailClientService.java` — `@Service`, inject Google OAuth2 client credentials from `application.yml` (clientId, clientSecret, redirectUri, scopes: gmail.readonly); implement `String buildAuthorizationUrl(UUID userId)` — generate Google OAuth2 consent URL with state=userId; implement `GoogleTokenResponse exchangeCode(String code)` — exchange authorization code for access+refresh tokens; implement `Gmail getGmailService(String accessToken)` — build authorized Gmail client instance
- [ ] T038 [US1] Create EmailAccountController at `src/main/java/com/notifications/controller/EmailAccountController.java` — `@RestController @RequestMapping("/api/v1/email-accounts")`, implement: POST `/connect` → call GmailClientService.buildAuthorizationUrl, return redirect URL; GET `/callback?code=&state=` → exchange code for tokens, save EmailAccount, return 201 with EmailAccountResponse; GET `/` → list user's email accounts; GET `/{id}` → get single account detail; DELETE `/{id}` → delete account and cascade; extract userId from SecurityContext for all endpoints
- [ ] T039 [US1] Create EmailAccountService interface at `src/main/java/com/notifications/service/email/EmailAccountService.java` — define: `EmailAccountResponse connectAccount(UUID userId, String code)`, `List<EmailAccountResponse> listAccounts(UUID userId)`, `EmailAccountResponse getAccount(UUID userId, UUID accountId)`, `void disconnectAccount(UUID userId, UUID accountId)`
- [ ] T040 [US1] Implement EmailAccountServiceImpl at `src/main/java/com/notifications/service/email/EmailAccountServiceImpl.java` — `@Service`, inject EmailAccountRepository, GmailClientService, EmailAccountMapper, EncryptionConverter; connectAccount: exchange code → encrypt tokens → save EmailAccount with status=active, provider=gmail → fetch initial historyId via `users.getProfile` → return mapped response; listAccounts/getAccount: query by userId → map to responses; disconnectAccount: find by id+userId → delete → revoke Gmail tokens
- [ ] T041 [US1] Add Gmail OAuth2 properties to `src/main/resources/application.yml` — add `gmail.oauth2.client-id`, `gmail.oauth2.client-secret`, `gmail.oauth2.redirect-uri` (http://localhost:8080/api/v1/email-accounts/callback), `gmail.oauth2.scopes` (https://www.googleapis.com/auth/gmail.readonly), `gmail.oauth2.token-uri`, `gmail.oauth2.auth-uri`

**Checkpoint**: Users can connect Gmail via OAuth2, list/view/disconnect email accounts. US1 is fully functional.

---

## Phase 4: User Story 2 — Receive Notifications for New Emails (Priority: P1) 🎯 MVP

**Goal**: System scans connected email accounts for new emails and delivers notifications to configured Slack/WhatsApp channels with deduplication and batching

**Independent Test**: Connect email + configure Slack channel → send test email → verify Slack notification received within 2 minutes with sender, subject, preview

### Implementation for User Story 2

- [ ] T042 [US2] Create NotificationSender interface (Strategy pattern) at `src/main/java/com/notifications/service/notification/NotificationSender.java` — define `String send(NotificationChannel channel, String senderName, String subject, String preview)` returning external message ID, `boolean supports(String channelType)`
- [ ] T043 [US2] Implement SlackNotificationSender at `src/main/java/com/notifications/service/notification/SlackNotificationSender.java` — `@Service`, inject Slack `AsyncMethodsClient`, apply `@CircuitBreaker(name="slack")` and `@RateLimiter(name="slack")` on send method; decrypt bot_token → build Block Kit message (section: "📧 New email from {sender}", section: "Subject: {subject}", section: "{preview}") → call `chatPostMessage(channel.slackChannelId)` → return Slack `ts` as externalMessageId; handle 429 → parse Retry-After → throw RateLimitExceededException; handle channel_not_found/token_revoked → throw NotificationDeliveryException
- [ ] T044 [US2] Implement WhatsAppNotificationSender at `src/main/java/com/notifications/service/notification/WhatsAppNotificationSender.java` — `@Service`, apply `@CircuitBreaker(name="whatsapp")` and `@RateLimiter(name="whatsapp")`; use Twilio `Message.creator()` with `whatsapp:` prefixed from/to numbers → send template message "📧 New email from {sender}\nSubject: {subject}\n{preview}" → return Twilio MessageSid as externalMessageId; configure Twilio credentials from `application.yml` (twilio.account-sid, twilio.auth-token, twilio.whatsapp.from-number)
- [ ] T045 [US2] Create NotificationDispatcher at `src/main/java/com/notifications/service/notification/NotificationDispatcher.java` — `@Service`, inject `List<NotificationSender>` (auto-discovers all implementations); implement `void dispatch(User user, EmailAccount account, String senderName, String senderAddress, String subject, String preview, Instant emailReceivedAt)` — find user's active NotificationChannels → for each channel, find matching sender via `supports(channelType)` → call `send()` → create Notification entity with deliveryStatus=sent, store externalMessageId → save via NotificationRepository; on failure: create Notification with deliveryStatus=failed, increment retryCount
- [ ] T046 [US2] Create DeduplicationService interface at `src/main/java/com/notifications/service/dedup/DeduplicationService.java` — define `boolean isDuplicate(UUID userId, String gmailMessageId)`, `void markProcessed(UUID userId, String gmailMessageId)`
- [ ] T047 [US2] Implement RedisDeduplicationServiceImpl at `src/main/java/com/notifications/service/dedup/RedisDeduplicationServiceImpl.java` — `@Service`, inject `RedisTemplate<String, String>`; isDuplicate: check `SISMEMBER` on key `dedup:{userId}` for gmailMessageId; markProcessed: `SADD` to `dedup:{userId}` + `EXPIRE` 48 hours (172800 seconds) if key is new; use Redis SET per research.md §7
- [ ] T048 [US2] Create EmailScannerService interface at `src/main/java/com/notifications/service/email/EmailScannerService.java` — define `void scanAccount(EmailAccount account)` — scan a single email account for new messages and trigger notifications
- [ ] T049 [US2] Implement EmailScannerServiceImpl at `src/main/java/com/notifications/service/email/EmailScannerServiceImpl.java` — `@Service`, inject GmailClientService, DeduplicationService, NotificationDispatcher, EmailAccountRepository; apply `@CircuitBreaker(name="gmail")` and `@RateLimiter(name="gmail")`; scanAccount: build Gmail client from decrypted access token → call `history.list(startHistoryId=account.historyId)` → for each new message ID, check dedup → call `messages.get(id, format=METADATA)` → extract sender, subject, snippet (truncate to 150 chars) → call dispatcher.dispatch() → mark as processed in dedup → update account.historyId and lastScannedAt; handle token expiry: use refresh token to get new access token → re-encrypt → update account; handle errors: set account status=error after 3 consecutive failures
- [ ] T050 [US2] Create EmailScanScheduler at `src/main/java/com/notifications/scheduler/EmailScanScheduler.java` — `@Component`, inject EmailAccountRepository and EmailScannerService; `@Scheduled(fixedDelayString="${gmail.scan.interval:60000}")` method `scanAllActiveAccounts()` — query all EmailAccounts with status=active → for each account, call `emailScannerService.scanAccount(account)` in try-catch (log errors, don't stop scanning other accounts); skip accounts where user.notificationsPaused=true
- [ ] T051 [US2] Create NotificationService interface at `src/main/java/com/notifications/service/notification/NotificationService.java` — define `Page<NotificationResponse> getNotificationHistory(UUID userId, Pageable pageable)`
- [ ] T052 [US2] Implement NotificationServiceImpl at `src/main/java/com/notifications/service/notification/NotificationServiceImpl.java` — `@Service`, inject NotificationRepository and NotificationMapper; getNotificationHistory: query `findByUserIdOrderByCreatedAtDesc` → map to NotificationResponse page
- [ ] T053 [US2] Create NotificationHistoryController — add GET `/api/v1/notifications` endpoint to a new controller at `src/main/java/com/notifications/controller/NotificationController.java` — `@RestController @RequestMapping("/api/v1/notifications")`, GET `/` with `@RequestParam defaultValue` for page/size → return paginated NotificationResponse list; extract userId from SecurityContext
- [ ] T054 [US2] Add Twilio/Slack properties to `src/main/resources/application.yml` — add `slack.bot-token` placeholder, `twilio.account-sid`, `twilio.auth-token`, `twilio.whatsapp.from-number` (whatsapp:+14155238886 sandbox), `gmail.scan.interval: 60000`

**Checkpoint**: Full email scanning → notification delivery pipeline works. Core MVP (US1 + US2) is complete.

---

## Phase 5: User Story 3 — Choose Notification Channel (Priority: P2)

**Goal**: Users can CRUD Slack and WhatsApp notification channels, controlling where they receive notifications

**Independent Test**: Create a Slack channel via POST /notification-channels, verify it appears in GET /notification-channels, update it, delete it

### Implementation for User Story 3

- [ ] T055 [US3] Create NotificationChannelService interface at `src/main/java/com/notifications/service/notification/NotificationChannelService.java` — define `NotificationChannelResponse createChannel(UUID userId, NotificationChannelRequest request)`, `List<NotificationChannelResponse> listChannels(UUID userId)`, `NotificationChannelResponse updateChannel(UUID userId, UUID channelId, NotificationChannelRequest request)`, `void deleteChannel(UUID userId, UUID channelId)`
- [ ] T056 [US3] Implement NotificationChannelServiceImpl at `src/main/java/com/notifications/service/notification/NotificationChannelServiceImpl.java` — `@Service`, inject NotificationChannelRepository, NotificationChannelMapper; createChannel: validate channelType-specific fields (slack needs botToken+slackChannelId, whatsapp needs phone+twilioSid+consent) → encrypt bot_token via @Convert → save → return response; listChannels: findByUserId → map; updateChannel: find by id+userId → update fields → save; deleteChannel: find by id+userId → delete; enforce UNIQUE(user_id, channel_type) — throw 409 on duplicate
- [ ] T057 [US3] Create NotificationChannelController at `src/main/java/com/notifications/controller/NotificationChannelController.java` — `@RestController @RequestMapping("/api/v1/notification-channels")`, POST `/` → 201 createChannel, GET `/` → 200 listChannels, PATCH `/{id}` → 200 updateChannel, DELETE `/{id}` → 204 deleteChannel; validate request body with `@Valid`; extract userId from SecurityContext

**Checkpoint**: Users can fully manage their notification channels. US3 is independently functional.

---

## Phase 6: User Story 4 — Pause and Resume Notifications (Priority: P3)

**Goal**: Users can pause and resume email scanning per account, preventing notifications during paused periods

**Independent Test**: PATCH /email-accounts/{id}/pause → verify status changes to "paused" and no notifications sent for new emails; PATCH /resume → verify scanning resumes

### Implementation for User Story 4

- [ ] T058 [US4] Add pause/resume methods to EmailAccountService interface at `src/main/java/com/notifications/service/email/EmailAccountService.java` — add `EmailAccountResponse pauseAccount(UUID userId, UUID accountId)`, `EmailAccountResponse resumeAccount(UUID userId, UUID accountId)`
- [ ] T059 [US4] Implement pause/resume in EmailAccountServiceImpl at `src/main/java/com/notifications/service/email/EmailAccountServiceImpl.java` — pauseAccount: find by id+userId → validate status is 'active' → set status='paused' → save → return response; resumeAccount: find by id+userId → validate status is 'paused' → set status='active' → save (scanning resumes from current historyId, no backfill) → return response; throw IllegalStateException with message if invalid transition
- [ ] T060 [US4] Add pause/resume endpoints to EmailAccountController at `src/main/java/com/notifications/controller/EmailAccountController.java` — PATCH `/{id}/pause` → call pauseAccount → 200, PATCH `/{id}/resume` → call resumeAccount → 200

**Checkpoint**: Users can pause/resume scanning. EmailScanScheduler already skips paused accounts (T050). US4 is complete.

---

## Phase 7: User Story 5 — Notification Filtering (Priority: P3)

**Goal**: Users can create filter rules (by sender or subject keyword) so only matching emails trigger notifications

**Independent Test**: Create a sender filter rule → send matching email → notification sent; send non-matching email → no notification; delete all rules → all emails trigger notifications (default)

### Implementation for User Story 5

- [ ] T061 [US5] Create FilterService interface at `src/main/java/com/notifications/service/filter/FilterService.java` — define `FilterRuleResponse createRule(UUID userId, FilterRuleRequest request)`, `List<FilterRuleResponse> listRules(UUID userId)`, `FilterRuleResponse updateRule(UUID userId, UUID ruleId, FilterRuleRequest request)`, `void deleteRule(UUID userId, UUID ruleId)`, `boolean shouldNotify(UUID userId, String senderAddress, String subject)`
- [ ] T062 [US5] Implement FilterServiceImpl at `src/main/java/com/notifications/service/filter/FilterServiceImpl.java` — `@Service`, inject FilterRuleRepository, FilterRuleMapper; CRUD: standard create/list/update/delete with userId scoping; shouldNotify: fetch active rules for userId → if no rules exist, return true (all emails notify per spec) → else iterate rules by priority: for 'sender' type check `senderAddress.equalsIgnoreCase(rule.pattern)`, for 'subject' type check `subject.toLowerCase().contains(rule.pattern.toLowerCase())` → return true if ANY rule matches, false if none match
- [ ] T063 [US5] Create FilterRuleController at `src/main/java/com/notifications/controller/FilterRuleController.java` — `@RestController @RequestMapping("/api/v1/filter-rules")`, POST `/` → 201 createRule, GET `/` → 200 listRules, PUT `/{id}` → 200 updateRule, DELETE `/{id}` → 204 deleteRule; validate request body with `@Valid`; extract userId from SecurityContext
- [ ] T064 [US5] Integrate FilterService into EmailScannerServiceImpl at `src/main/java/com/notifications/service/email/EmailScannerServiceImpl.java` — inject FilterService; after extracting email metadata and before calling dispatcher.dispatch(), call `filterService.shouldNotify(userId, senderAddress, subject)` → skip dispatch if returns false; still mark as processed in dedup to avoid re-evaluation

**Checkpoint**: Filter rules control which emails trigger notifications. US5 is complete.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories — batching, retry, logging, health checks

- [ ] T065 [P] Implement notification batching logic in NotificationDispatcher at `src/main/java/com/notifications/service/notification/NotificationDispatcher.java` — add `ConcurrentHashMap<UUID, BatchWindow>` (userId → list of pending notifications + window start time); in dispatch(): if >10 notifications in 1-minute window → set deliveryStatus=batched, buffer notifications → when window closes (via scheduled flush), compose summary message ("📧 You have {count} new emails") with top 5 senders/subjects → send single summary notification; add `@Scheduled(fixedRate=60000)` method `flushBatchWindows()` to process expired windows
- [ ] T066 [P] Implement notification retry scheduler at `src/main/java/com/notifications/scheduler/NotificationRetryScheduler.java` — `@Component @Scheduled(fixedDelay=30000)`, query notifications with deliveryStatus=failed and retryCount < 3 → for each, attempt re-delivery via NotificationDispatcher → on success set status=sent → on failure increment retryCount; exponential backoff: only retry if `updatedAt + (30s * 2^retryCount)` has passed
- [ ] T067 [P] Add Actuator health indicators and Resilience4j endpoints — ensure `application.yml` exposes `/actuator/health` (includes circuitbreaker health indicators per ResilienceConfig), `/actuator/circuitbreakers`, `/actuator/ratelimiters`; configure `management.endpoints.web.exposure.include=health,info,circuitbreakers,ratelimiters` in `src/main/resources/application.yml`
- [ ] T068 [P] Add structured logging throughout services — use SLF4J with MDC (Mapped Diagnostic Context) to include userId and accountId in log context; add INFO-level logs for: email account connected/disconnected, scan started/completed, notification sent/failed/batched, channel created/deleted, filter rule changes; add WARN-level for circuit breaker state changes and rate limit hits; add ERROR-level for unrecoverable failures; configure log pattern in `src/main/resources/application.yml`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Setup (T001) — BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Phase 2 completion (entities, repos, auth, config)
- **User Story 2 (Phase 4)**: Depends on Phase 2 + Phase 3 (needs EmailAccount to exist)
- **User Story 3 (Phase 5)**: Depends on Phase 2 only — can run in PARALLEL with US1
- **User Story 4 (Phase 6)**: Depends on Phase 3 (extends EmailAccountService)
- **User Story 5 (Phase 7)**: Depends on Phase 4 (integrates into EmailScannerServiceImpl)
- **Polish (Phase 8)**: Depends on Phases 3-7 being substantially complete

### User Story Dependencies

```
Phase 1 (Setup)
    └── Phase 2 (Foundational) ← BLOCKS ALL
            ├── Phase 3 (US1: Connect Email) ──┐
            │       └── Phase 4 (US2: Scan & Notify) ──┐
            │               └── Phase 7 (US5: Filtering)│
            ├── Phase 5 (US3: Channels) ────────────────┤ (parallel with US1)
            └── Phase 6 (US4: Pause/Resume) ────────────┘ (after US1)
                                                         └── Phase 8 (Polish)
```

### Within Each User Story

- Interfaces before implementations
- Services before controllers
- Config/properties alongside service that consumes them
- Integration tasks (e.g., T064) last within each story

---

## Parallel Examples

### Phase 2 Parallel Groups

```text
# Group A — Migrations (all parallel after T008):
T009, T010, T011, T012

# Group B — Entities (all parallel after T008):
T013, T014, T015, T016, T017

# Group C — Repositories (all parallel after entities):
T018, T019, T020, T021, T022

# Group D — Config (all parallel, no inter-dependencies):
T023, T026, T027, T028, T029, T030, T031

# Group E — DTOs/Mappers (parallel with Group D):
T032, T033
```

### Cross-Story Parallelism

```text
# After Phase 2 completes, these can proceed simultaneously:
Developer A: Phase 3 (US1) → T037, T038, T039, T040, T041
Developer B: Phase 5 (US3) → T055, T056, T057
```

### Phase 8 Parallel Tasks

```text
# All Polish tasks are independent:
T065, T066, T067, T068
```

---

## Implementation Strategy

### MVP First (US1 + US2)

1. Complete Phase 1: Setup (T001–T007)
2. Complete Phase 2: Foundational (T008–T036)
3. Complete Phase 3: US1 — Connect Email (T037–T041)
4. Complete Phase 4: US2 — Scan & Notify (T042–T054)
5. **STOP and VALIDATE**: Connect Gmail → send test email → verify Slack/WhatsApp notification
6. Deploy/demo if ready — this is the core product

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 → Connect Gmail accounts → Deploy/Demo (foundation)
3. Add US2 → Scan & deliver notifications → Deploy/Demo (MVP!)
4. Add US3 → Channel management CRUD → Deploy/Demo
5. Add US4 → Pause/resume scanning → Deploy/Demo
6. Add US5 → Filtering rules → Deploy/Demo
7. Polish → Batching, retry, observability → Deploy/Demo (production-ready)

### Single Developer Strategy

1. Phases 1-2 sequentially (foundation — ~T001-T036)
2. Phase 3 (US1) → validate Gmail OAuth2 flow works
3. Phase 4 (US2) → validate end-to-end notification delivery
4. Phase 5 (US3) → channel management
5. Phase 6 (US4) → pause/resume
6. Phase 7 (US5) → filtering
7. Phase 8 → polish and production readiness

---

## Notes

- [P] tasks = different files, no dependencies — safe to parallelize
- [USx] label maps task to specific user story for traceability
- Total tasks: 68 (7 setup + 29 foundational + 5 US1 + 13 US2 + 3 US3 + 3 US4 + 4 US5 + 4 polish)
- No test tasks included per user request
- Commit after each task or logical group
- Stop at any checkpoint to validate the story independently
