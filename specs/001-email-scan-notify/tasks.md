# Tasks: Email Scan & Mobile Notifications

**Input**: Design documents from `/specs/001-email-scan-notify/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/api.yaml ✅, quickstart.md ✅

**Tests**: Not explicitly requested — test tasks are omitted.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

**Status Legend**: ✅ = Done (implemented in prior sessions), 🆕 = New task

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `src/main/java/com/notifications/` (Spring Boot)
- **Frontend**: `frontend/src/` (React + Vite)
- **Migrations**: `src/main/resources/db/migration/`
- **Config**: `src/main/resources/`

---

## Phase 1: Setup (Shared Infrastructure) ✅

**Purpose**: Project initialization, build tooling, Docker, and basic Spring Boot scaffolding

- [x] T001 Create project structure with Maven `pom.xml` including all dependencies in `pom.xml`
- [x] T002 [P] Create `Dockerfile` with multi-stage build in `Dockerfile`
- [x] T003 [P] Create `docker-compose.yml` with PostgreSQL 16 and Redis 7 services in `docker-compose.yml`
- [x] T004 [P] Create `.gitignore` with Java/Maven/IDE/Docker exclusions in `.gitignore`
- [x] T005 Create Spring Boot main application class in `src/main/java/com/notifications/NotificationsApplication.java`
- [x] T006 [P] Create `application.yml` with base config in `src/main/resources/application.yml`
- [x] T007 [P] Create `application-dev.yml` with dev-specific config in `src/main/resources/application-dev.yml`
- [x] T008 [P] Create `application-prod.yml` with production config in `src/main/resources/application-prod.yml`

---

## Phase 2: Foundational (Blocking Prerequisites) ✅

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

### Database Migrations ✅

- [x] T009 Create Flyway migration V1 for `users` table in `src/main/resources/db/migration/V1__create_users.sql`
- [x] T010 Create Flyway migration V2 for `email_accounts` table in `src/main/resources/db/migration/V2__create_email_accounts.sql`
- [x] T011 Create Flyway migration V3 for `notification_channels` table in `src/main/resources/db/migration/V3__create_notification_channels.sql`
- [x] T012 Create Flyway migration V4 for `notifications` table in `src/main/resources/db/migration/V4__create_notifications.sql`
- [x] T013 Create Flyway migration V5 for `filter_rules` table in `src/main/resources/db/migration/V5__create_filter_rules.sql`

### Domain Entities ✅

- [x] T014 [P] Create User JPA entity in `src/main/java/com/notifications/domain/User.java`
- [x] T015 [P] Create EmailAccount JPA entity in `src/main/java/com/notifications/domain/EmailAccount.java`
- [x] T016 [P] Create NotificationChannel JPA entity in `src/main/java/com/notifications/domain/NotificationChannel.java`
- [x] T017 [P] Create Notification JPA entity in `src/main/java/com/notifications/domain/Notification.java`
- [x] T018 [P] Create FilterRule JPA entity in `src/main/java/com/notifications/domain/FilterRule.java`

### Repositories ✅

- [x] T019 [P] Create UserRepository in `src/main/java/com/notifications/repository/UserRepository.java`
- [x] T020 [P] Create EmailAccountRepository in `src/main/java/com/notifications/repository/EmailAccountRepository.java`
- [x] T021 [P] Create NotificationChannelRepository in `src/main/java/com/notifications/repository/NotificationChannelRepository.java`
- [x] T022 [P] Create NotificationRepository in `src/main/java/com/notifications/repository/NotificationRepository.java`
- [x] T023 [P] Create FilterRuleRepository in `src/main/java/com/notifications/repository/FilterRuleRepository.java`

### Security & Auth Infrastructure ✅

- [x] T024 Create AES-256-GCM encryption JPA AttributeConverter in `src/main/java/com/notifications/config/EncryptionConverter.java`
- [x] T025 Create JWT token provider in `src/main/java/com/notifications/config/JwtTokenProvider.java`
- [x] T026 Create JWT authentication filter in `src/main/java/com/notifications/config/JwtAuthenticationFilter.java`
- [x] T027 Create SecurityConfig in `src/main/java/com/notifications/config/SecurityConfig.java`

### Resilience & Cross-Cutting Config ✅

- [x] T028 [P] Create RedisConfig in `src/main/java/com/notifications/config/RedisConfig.java`
- [x] T029 [P] Create SchedulerConfig in `src/main/java/com/notifications/config/SchedulerConfig.java`
- [x] T030 [P] Create WebConfig in `src/main/java/com/notifications/config/WebConfig.java`
- [x] T031 Configure Resilience4j circuit breakers in `src/main/resources/application.yml`
- [x] T032 Configure Resilience4j rate limiters in `src/main/resources/application.yml`

### DTOs & Mappers ✅

- [x] T033 [P] Create request DTOs: RegisterRequest, LoginRequest, RefreshTokenRequest in `src/main/java/com/notifications/dto/request/`
- [x] T034 [P] Create request DTOs: NotificationChannelRequest, FilterRuleRequest in `src/main/java/com/notifications/dto/request/`
- [x] T035 [P] Create response DTOs: AuthTokenResponse, EmailAccountResponse, NotificationChannelResponse, NotificationResponse, FilterRuleResponse in `src/main/java/com/notifications/dto/response/`
- [x] T036 [P] Create MapStruct mappers in `src/main/java/com/notifications/mapper/`

### Exception Handling ✅

- [x] T037 [P] Create custom exceptions in `src/main/java/com/notifications/exception/`
- [x] T038 Create GlobalExceptionHandler in `src/main/java/com/notifications/controller/GlobalExceptionHandler.java`

### Auth Service & Controller ✅

- [x] T039 Create AuthService interface in `src/main/java/com/notifications/service/auth/AuthService.java`
- [x] T040 Create AuthServiceImpl in `src/main/java/com/notifications/service/auth/AuthServiceImpl.java`
- [x] T041 Create AuthController in `src/main/java/com/notifications/controller/AuthController.java`

---

## Phase 3: User Story 1 — Connect Email Account (Priority: P1) ✅

- [x] T042–T048 All US1 backend tasks complete (GmailClientService, EmailAccountService, EmailScanScheduler, etc.)

---

## Phase 4: User Story 2 — Receive Notifications for New Emails (Priority: P1) ✅

- [x] T049–T059 All US2 backend tasks complete (Dedup, NotificationSender, SlackSender, WhatsAppSender, Dispatcher, etc.)

---

## Phase 5: User Story 3 — Choose Notification Channel (Priority: P2) ✅

- [x] T060–T062 All US3 backend tasks complete (NotificationChannelService, Controller with PATCH endpoint)

---

## Phase 6: User Story 4 — Pause and Resume Notifications (Priority: P3) ✅

- [x] T063–T065 All US4 backend tasks complete (pause/resume endpoints, scheduler skip logic)

---

## Phase 7: User Story 5 — Notification Filtering (Priority: P3) ✅

- [x] T066–T069 All US5 backend tasks complete (FilterService, FilterRuleController, scanner integration)

---

## Phase 8: Frontend Application (React + Vite + TailwindCSS) ✅

- [x] T070–T090 All frontend base tasks complete (Setup, Auth, API layer, Pages, Routing)

---

## Phase 9: Polish & Cross-Cutting Concerns ✅

- [x] T091–T096 Original polish tasks complete

---

## Phase 10: UX Improvements & Missing Features 🆕

**Purpose**: Address usability gaps identified during user testing — edit capabilities, error UX, auth flows, and profile features

**⚠️ PRIORITY**: These are high-impact UX issues affecting everyday usability

### User Story 6 — Edit Notification Channels (Priority: P1) 🆕

**Goal**: Users can edit existing notification channel details (e.g., update WhatsApp phone number, Slack channel ID) without deleting and recreating

**Independent Test**: Add a WhatsApp channel, click edit, change the phone number, save, verify the updated number appears in the channel list

- [ ] T097 [US6] Add edit button (Pencil icon already imported) to each channel card in the channel list, wire onClick to open an edit modal pre-populated with current channel data (channelType, slackChannelId, whatsappPhoneNumber, consentGiven) in `frontend/src/pages/ChannelsPage.jsx`
- [ ] T098 [US6] Create EditChannelModal component reusing the add-channel form fields, with pre-filled values from the selected channel, call `updateChannel(id, data)` from `frontend/src/api/channels.js` on save, show success/error toast in `frontend/src/pages/ChannelsPage.jsx`
- [ ] T099 [P] [US6] Add `NotificationChannelUpdateRequest` DTO if not already present — allow partial updates (phone number, slack channel ID, consent) without requiring all fields in `src/main/java/com/notifications/dto/request/NotificationChannelRequest.java`

**Checkpoint**: Users can edit channel details inline without delete/recreate

---

### User Story 7 — Forgot Password Flow (Priority: P1) 🆕

**Goal**: Users who forget their password can request a reset link via email and set a new password

**Independent Test**: Click "Forgot password?" on login page, enter email, verify reset token is generated (check logs in dev), use token to set new password, verify login works with new password

#### Backend

- [ ] T100 [US7] Add `password_reset_token` (VARCHAR 128, nullable) and `password_reset_expires_at` (TIMESTAMPTZ, nullable) columns to users table via Flyway migration V6 in `src/main/resources/db/migration/V6__add_password_reset_columns.sql`
- [ ] T101 [US7] Update User JPA entity with `passwordResetToken` and `passwordResetExpiresAt` fields in `src/main/java/com/notifications/domain/User.java`
- [ ] T102 [US7] Add `findByPasswordResetToken` query to UserRepository in `src/main/java/com/notifications/repository/UserRepository.java`
- [ ] T103 [US7] Add `forgotPassword(email)` method to AuthService — generate secure random token (UUID), set 1-hour expiry, persist to user row, log the reset URL in dev mode (email sending is out of scope for MVP) in `src/main/java/com/notifications/service/auth/AuthServiceImpl.java`
- [ ] T104 [US7] Add `resetPassword(token, newPassword)` method to AuthService — validate token exists and not expired, hash new password with BCrypt, clear reset token fields, return success in `src/main/java/com/notifications/service/auth/AuthServiceImpl.java`
- [ ] T105 [US7] Add POST `/api/v1/auth/forgot-password` (accepts email, returns 200 always for security) and POST `/api/v1/auth/reset-password` (accepts token + newPassword, returns 200 on success) to AuthController with @RateLimiter(name="inbound-api") in `src/main/java/com/notifications/controller/AuthController.java`

#### Frontend

- [ ] T106 [P] [US7] Add "Forgot password?" link below the password field on LoginPage, linking to `/forgot-password` route in `frontend/src/pages/LoginPage.jsx`
- [ ] T107 [US7] Create ForgotPasswordPage with email input form, calls POST `/api/v1/auth/forgot-password`, shows success message "If an account exists, a reset link has been sent" regardless of response in `frontend/src/pages/ForgotPasswordPage.jsx`
- [ ] T108 [US7] Create ResetPasswordPage that reads token from URL query param, shows new password + confirm password form, calls POST `/api/v1/auth/reset-password`, redirects to login on success in `frontend/src/pages/ResetPasswordPage.jsx`
- [ ] T109 [US7] Add `/forgot-password` and `/reset-password` routes (public, no auth required) to App.jsx router configuration in `frontend/src/App.jsx`
- [ ] T110 [P] [US7] Create `auth` API service functions: `forgotPassword(email)` and `resetPassword(token, newPassword)` in `frontend/src/api/auth.js`

**Checkpoint**: Full forgot/reset password flow works end-to-end

---

### User Story 8 — Proper Error Handling & Auto-Signout (Priority: P1) 🆕

**Goal**: API errors display meaningful messages in the UI, error banners are dismissable, and 401/403 responses trigger automatic signout instead of showing raw error codes

**Independent Test**: Let JWT expire, make any API call, verify auto-redirect to login page (not a 403 error). Trigger a validation error, verify the exact backend error message appears in the toast. Verify error toasts have a close (X) button.

#### Frontend Error Infrastructure

- [ ] T111 [US8] Add 403 handling to Axios response interceptor — on 403 status, clear localStorage tokens and redirect to `/login` (same behavior as 401 with expired refresh token) in `frontend/src/api/client.js`
- [ ] T112 [US8] Improve error message extraction in Axios interceptor — create a `getErrorMessage(error)` utility that extracts `error.response.data.message` (backend structured error), falls back to `error.response.statusText`, then to a generic "Something went wrong" message in `frontend/src/utils/errorUtils.js`
- [ ] T113 [P] [US8] Update all page components to use `getErrorMessage(err)` utility instead of inline `err.response?.data?.message || 'hardcoded fallback'` pattern in `frontend/src/pages/EmailAccountsPage.jsx`, `frontend/src/pages/ChannelsPage.jsx`, `frontend/src/pages/FiltersPage.jsx`, `frontend/src/pages/NotificationsPage.jsx`, `frontend/src/pages/DashboardPage.jsx`
- [ ] T114 [US8] Configure react-hot-toast Toaster with dismiss button — set `toastOptions` with `duration: 5000` and custom render that includes a close (X) button on all toast types (success, error, loading) in `frontend/src/main.jsx`
- [ ] T115 [P] [US8] Add a global error event listener in AuthContext — listen for a custom `auth:signout` event dispatched by the Axios interceptor, call `logout()` to clear state and redirect cleanly (avoids stale React state after window.location.href redirect) in `frontend/src/context/AuthContext.jsx`

**Checkpoint**: All API errors show meaningful messages, expired sessions auto-logout, toasts are dismissable

---

### User Story 9 — User Profile & Avatar (Priority: P2) 🆕

**Goal**: Users can view their profile, see their avatar (Gravatar-based), and the sidebar shows user identity

**Independent Test**: Register, login, verify sidebar shows user email and Gravatar image. Navigate to profile page, verify email and account creation date are shown.

#### Backend

- [ ] T116 [US9] Add GET `/api/v1/users/me` endpoint to return current user's profile (id, email, notificationsPaused, createdAt) — create UserController or add to AuthController in `src/main/java/com/notifications/controller/UserController.java`
- [ ] T117 [P] [US9] Create UserProfileResponse DTO with id, email, notificationsPaused, createdAt, gravatarUrl (computed from email MD5 hash → `https://www.gravatar.com/avatar/{md5}?d=identicon&s=80`) in `src/main/java/com/notifications/dto/response/UserProfileResponse.java`

#### Frontend

- [ ] T118 [US9] Create `user` API service with `getProfile()` calling GET `/api/v1/users/me` in `frontend/src/api/user.js`
- [ ] T119 [US9] Update AuthContext to fetch and cache user profile on login/page load — store `user` object (email, gravatarUrl) in context state, provide `user` via context value in `frontend/src/context/AuthContext.jsx`
- [ ] T120 [US9] Update Sidebar to show user avatar (Gravatar image) and email between the nav links and logout button — use `user` from AuthContext in `frontend/src/components/Sidebar.jsx`
- [ ] T121 [US9] Create ProfilePage showing user email, avatar (large Gravatar), account creation date, notification pause status toggle in `frontend/src/pages/ProfilePage.jsx`
- [ ] T122 [US9] Add `/profile` route to App.jsx (protected) and add Profile link to Sidebar navigation in `frontend/src/App.jsx` and `frontend/src/components/Sidebar.jsx`

**Checkpoint**: User identity visible in sidebar, profile page accessible

---

### User Story 10 — Gmail API Test User Configuration (Priority: P2) 🆕

**Goal**: Provide a way to add test users to the Gmail API OAuth consent screen so other team members can test the OAuth flow without being blocked by Google's "app not verified" restrictions

**Independent Test**: Follow the documented steps to add a test user in Google Cloud Console, then verify that user can complete the Gmail OAuth2 connect flow successfully

- [ ] T123 [US10] Add "Adding Test Users" section to quickstart.md documenting: (1) Navigate to Google Cloud Console → APIs & Services → OAuth consent screen → Test users, (2) Click "Add users", (3) Enter the Gmail address of the test user, (4) Save — the test user can now complete the OAuth consent flow for unverified apps in `specs/001-email-scan-notify/quickstart.md`
- [ ] T124 [P] [US10] Add a developer-facing info banner on EmailAccountsPage that shows when no email accounts are connected, explaining: "Your Google Cloud project may be in 'Testing' mode. Only test users added in the OAuth consent screen can connect. See quickstart.md for setup." in `frontend/src/pages/EmailAccountsPage.jsx`

**Checkpoint**: Test user setup documented, developer-facing guidance shown in UI

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phases 1–9**: ✅ Complete
- **Phase 10 (UX Improvements)**: Can start immediately — all backend/frontend infrastructure exists

### User Story Dependencies (Phase 10)

```
Phase 10: UX Improvements (all can start in parallel)
    ├── US6 (Edit Channels): T097–T099 — independent, frontend + minor backend
    ├── US7 (Forgot Password): T100–T110 — independent, full-stack
    │       T100 (migration) → T101 (entity) → T102 (repo) → T103–T104 (service) → T105 (controller)
    │       T106–T110 (frontend) can start in parallel after T105
    ├── US8 (Error Handling): T111–T115 — independent, frontend-only
    │       T111 + T112 first → T113 depends on T112 → T114, T115 parallel
    ├── US9 (Profile & Avatar): T116–T122 — independent, full-stack
    │       T116–T117 (backend) → T118–T122 (frontend sequential)
    └── US10 (Gmail Test Users): T123–T124 — independent, docs + frontend
```

### Parallel Opportunities (Phase 10)

- **All 5 user stories** (US6–US10) can run **fully in parallel** — they touch different files and features
- **Within US7**: Backend (T100–T105) and frontend (T106–T110) are sequential within each track, but frontend can start T106 in parallel with backend
- **Within US8**: T111 + T112 parallel, then T113–T115 parallel after T112 completes
- **Within US9**: T116 + T117 parallel (backend), then T118–T122 sequential (frontend)
- **US6 + US10**: Quickest wins — 2–3 tasks each, can complete first

---

## Parallel Example: Phase 10

```
# Launch all 5 user stories simultaneously (different files, independent):
Stream 1 (US6): T097 → T098 → T099  (Edit Channels — 3 tasks)
Stream 2 (US7): T100 → T101 → T102 → T103 → T104 → T105 | T106–T110  (Forgot Password — 11 tasks)
Stream 3 (US8): T111 + T112 → T113 + T114 + T115  (Error Handling — 5 tasks)
Stream 4 (US9): T116 + T117 → T118 → T119 → T120 → T121 → T122  (Profile — 7 tasks)
Stream 5 (US10): T123 + T124  (Gmail Test Users — 2 tasks)
```

---

## Implementation Strategy

### Completed (Phases 1–9)

1. ✅ Setup + Foundational → Foundation ready
2. ✅ User Stories 1–5 → All backend features complete
3. ✅ Frontend → All pages and routing complete
4. ✅ Polish → Batching, retention, health checks

### Phase 10: UX Improvements (Current Sprint)

**Recommended order by impact:**

1. **US8 (Error Handling & Auto-Signout)** — Fixes the most visible UX pain (403 errors, bad messages)
2. **US6 (Edit Channels)** — Quick win, 3 tasks, high user impact
3. **US7 (Forgot Password)** — Complete auth flow, 11 tasks
4. **US9 (Profile & Avatar)** — User identity and personalization
5. **US10 (Gmail Test Users)** — Documentation and onboarding

**All can run in parallel if multiple developers available.**

---

## Summary

| Phase | Tasks | Status |
|-------|-------|--------|
| Phase 1: Setup | T001–T008 (8) | ✅ Done |
| Phase 2: Foundational | T009–T041 (33) | ✅ Done |
| Phase 3: US1 Connect Email | T042–T048 (7) | ✅ Done |
| Phase 4: US2 Notifications | T049–T059 (11) | ✅ Done |
| Phase 5: US3 Channels | T060–T062 (3) | ✅ Done |
| Phase 6: US4 Pause/Resume | T063–T065 (3) | ✅ Done |
| Phase 7: US5 Filtering | T066–T069 (4) | ✅ Done |
| Phase 8: Frontend | T070–T090 (21) | ✅ Done |
| Phase 9: Polish | T091–T096 (6) | ✅ Done |
| **Phase 10: UX Improvements** | **T097–T124 (28)** | **🆕 New** |
| **Total** | **124 tasks** | **96 done, 28 new** |

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate the story independently
- Resilience4j annotations (@CircuitBreaker, @RateLimiter) must be on every external API call
- All sensitive data (OAuth tokens, bot tokens) encrypted at rest via EncryptionConverter
- Rate limit headers (X-RateLimit-Limit/Remaining/Reset) on all API responses
- Phase 10 tasks reference existing files — verify current code before modifying
