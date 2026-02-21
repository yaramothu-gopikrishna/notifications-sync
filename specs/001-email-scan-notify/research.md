# Research: Email Scan & Mobile Notifications

**Branch**: `001-email-scan-notify` | **Date**: 2026-02-21 | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

---

## 1. Gmail API Integration

**Decision**: Use the **Gmail API with Cloud Pub/Sub push notifications** as the primary mechanism, with `history.list` polling as a fallback.

**Rationale**: The Gmail API + Pub/Sub approach is superior to IMAP for several reasons:
- **Push-based**: Gmail sends change notifications to a Cloud Pub/Sub topic, eliminating the need for constant polling. The service subscribes to the topic and receives near-real-time notifications (typically <5 seconds).
- **OAuth2 native**: Gmail API uses Google's OAuth2 infrastructure directly, whereas IMAP+OAuth2 (XOAUTH2) is a bolted-on extension that Google has been deprioritizing.
- **Structured data**: Gmail API returns JSON with parsed headers, labels, and metadata. IMAP returns raw RFC 2822 messages requiring parsing.
- **Quota headroom**: At 250 quota units/user/second and 1 billion/day, the Gmail API is generous. A `users.messages.get` costs 5 units; `users.history.list` costs 2 units. For 1,000 users, even polling every minute uses only ~120,000 units/day — well under limits.
- **Watch mechanism**: Call `users.watch()` to register Pub/Sub notifications per user. Must be renewed every 7 days (recommend daily renewal via scheduler). On notification, call `history.list` with the last known `historyId` to fetch only new messages.

**Alternatives Considered**:
| Alternative | Why Not |
|---|---|
| **IMAP with IDLE** | Requires persistent TCP connections per user — doesn't scale to 1,000+ users. Google has restricted IMAP access for non-Google-Workspace accounts. No structured metadata. |
| **IMAP polling** | High latency (polling interval), high connection overhead, raw message parsing needed, Google deprioritizing IMAP OAuth2 support. |
| **Gmail API polling only** (no Pub/Sub) | Works but wastes quota on empty polls. At 1-min intervals for 1,000 users, that's ~2.88M quota units/day just for polling — unnecessary when push is available. |

**Key Configuration/Notes**:
```yaml
# Gmail API Quotas to respect
gmail:
  api:
    # users.messages.get = 5 units, users.history.list = 2 units
    # users.watch = 100 units (called once/day per user)
    quota-units-per-user-per-second: 250
    quota-units-per-day: 1000000000
    watch-renewal-interval: P1D  # Renew watch daily (expires in 7 days)
    history-poll-fallback-interval: PT2M  # Fallback if Pub/Sub misses
    max-messages-per-sync: 100  # Limit per history.list call
```
- **Implementation flow**: `users.watch()` → Pub/Sub notification → `history.list(startHistoryId)` → `messages.get(id, format=METADATA)` → dispatch notification.
- **Spring Boot integration**: Use `spring-cloud-gcp-starter-pubsub` for Pub/Sub subscription. Use `google-api-services-gmail` v1 client library with `google-auth-library-oauth2-http` for OAuth2 credential management.
- **Fallback**: Schedule a periodic `history.list` poll every 2 minutes as a safety net in case Pub/Sub delivery fails or is delayed.

---

## 2. Slack Integration

**Decision**: Use the **Slack Web API (`chat.postMessage`)** via the official `slack-api-client` Java SDK (`com.slack.api:slack-api-client`).

**Rationale**:
- **Rich messaging**: The Web API supports Block Kit for rich formatting (sender, subject, preview with sections/dividers), interactive buttons (e.g., "Mark as Read"), and threading for batched summaries.
- **Per-channel delivery**: Notifications can target DMs, channels, or group conversations — essential for user preference flexibility.
- **OAuth2 bot tokens**: The Web API uses bot tokens (`xoxb-`) installed per workspace. This is the modern, supported approach. Tokens don't expire (only revoked), simplifying token management vs. Incoming Webhooks which are per-channel and less flexible.
- **Rate limits manageable**: `chat.postMessage` allows ~1 message/second/channel with burst tolerance. For our use case (notification delivery, not mass messaging), this is sufficient. The Resilience4j rate limiter will enforce this.

**Alternatives Considered**:
| Alternative | Why Not |
|---|---|
| **Incoming Webhooks** | Fixed to a single channel per webhook URL. No threading, no Block Kit interactivity, no DM support. Rate limit is the same (1/sec/channel). Users would need to generate a webhook URL manually — poor UX. |
| **Slack Events API + Socket Mode** | Designed for receiving events from Slack, not sending messages. Not applicable for our outbound notification use case. |
| **Third-party libraries (e.g., jSlack)** | Unmaintained. The official `com.slack.api:slack-api-client` is actively maintained by Slack and covers the full API surface. |

**Key Configuration/Notes**:
```yaml
slack:
  api:
    # Rate limits
    post-message-rate: 1      # 1 message/sec/channel (Slack enforced)
    burst-tolerance: 3         # Short bursts >1 allowed
    workspace-limit-per-min: 300  # Workspace-wide approximate limit
    retry-after-header: true   # Respect Retry-After on HTTP 429
  sdk:
    dependency: "com.slack.api:slack-api-client:1.44.x"
    async-client: true         # Use AsyncMethodsClient for non-blocking calls
```
- **Implementation**: Use `AsyncMethodsClient.chatPostMessage()` for non-blocking delivery. Store bot token per user's connected workspace in `notification_channels` table.
- **Message format**: Use Block Kit JSON with `section` (sender + subject), `context` (timestamp), and `section` (preview text truncated to 150 chars).
- **Error handling**: On HTTP 429, parse `Retry-After` header and queue for retry. On `channel_not_found` or `token_revoked`, mark the notification channel as errored and alert the user.

---

## 3. WhatsApp Integration

**Decision**: Use **Twilio WhatsApp API** via the `twilio-java` SDK (`com.twilio.sdk:twilio`).

**Rationale**:
- **Simpler onboarding**: Twilio provides a managed WhatsApp Business API experience — phone number provisioning, message template approval, and compliance are handled through Twilio's console. Meta Cloud API requires direct Meta Business Manager setup, WABA creation, and manual phone number verification.
- **Unified API**: Twilio's Programmable Messaging API uses the same `Messages.creator()` interface for SMS, WhatsApp, and other channels. If we add SMS notifications later, no new SDK or integration pattern is needed.
- **Template management**: WhatsApp requires pre-approved message templates for business-initiated (notification) messages. Twilio manages the template approval workflow and provides a Content API for template management.
- **Java SDK maturity**: `twilio-java` is well-maintained, thread-safe, and works seamlessly with Spring Boot. Meta's Cloud API has no official Java SDK — would require manual HTTP client implementation.
- **Pricing**: Twilio charges per-message on top of Meta's conversation-based pricing. For a notification service at moderate scale (~1,000 users, ~50 notifications/day each), the Twilio markup is acceptable for the reduced operational complexity. Approximate cost: $0.005–0.05/message depending on region (utility conversations).

**Alternatives Considered**:
| Alternative | Why Not |
|---|---|
| **Meta Cloud API (direct)** | No official Java SDK. Requires managing webhook verification, phone number registration, and template approval directly with Meta. More complex setup, but lower per-message cost at scale. Better choice if scaling to 100K+ users. |
| **MessageBird / Vonage** | Smaller market share, less documentation, fewer community examples. Twilio is the industry standard for programmable messaging. |

**Key Configuration/Notes**:
```yaml
whatsapp:
  provider: twilio
  api:
    # Rate limits (Twilio WhatsApp)
    messages-per-second: 80          # Twilio default throughput per number
    concurrent-requests: 10          # Connection pool limit
    template-sid: "HXxxxxxxxxx"      # Pre-approved notification template SID
    content-sid: "HXxxxxxxxxx"       # Content template for rich notifications
  pricing:
    # WhatsApp conversation-based pricing (Meta) + Twilio markup
    # Utility conversations: ~$0.005-0.05/conversation (24-hour window)
    # Each conversation window allows unlimited messages for 24 hours
    model: "per-conversation"
```
- **Message templates**: Pre-register a notification template: `"📧 New email from {{1}}\nSubject: {{2}}\n{{3}}"` where `{{1}}` = sender, `{{2}}` = subject, `{{3}}` = preview.
- **Implementation**: Use `Message.creator(to, from, body)` with `whatsapp:` prefixed phone numbers. For template messages, use Content API with `ContentSid`.
- **Opt-in requirement**: WhatsApp mandates explicit user opt-in before sending business messages. Store opt-in consent with timestamp in the `notification_channels` table.

---

## 4. Resilience4j Circuit Breaker Configuration

**Decision**: Configure **separate circuit breaker instances per external provider** (Gmail, Slack, WhatsApp) using a **count-based sliding window** with provider-tuned thresholds.

**Rationale**:
- **Isolation**: Each external API has different failure characteristics. Gmail may have transient 503s during maintenance; Slack may rate-limit aggressively; WhatsApp/Twilio may have regional outages. Separate circuit breakers prevent a Slack outage from blocking Gmail scanning or WhatsApp delivery.
- **Count-based window**: Preferable to time-based for our use case because notification delivery is event-driven (not constant traffic). A time-based window might not accumulate enough calls to calculate a meaningful failure rate during low-traffic periods.
- **Conservative thresholds**: Since notifications are the core value proposition, we want to be cautious about opening the circuit. A 50% failure rate threshold with a minimum of 10 calls prevents premature tripping on a few transient errors.

**Alternatives Considered**:
| Alternative | Why Not |
|---|---|
| **Single shared circuit breaker** | A failure in one provider would block all providers. Violates fault isolation principle. |
| **Time-based sliding window** | During off-peak hours with few calls, the window may not fill enough to calculate reliable failure rates. Count-based is more predictable. |
| **No circuit breaker (retry only)** | Retrying a down service wastes resources and can cascade failures. Circuit breaker fast-fails and gives the service time to recover. |

**Key Configuration/Notes**:
```yaml
resilience4j:
  circuitbreaker:
    configs:
      # Shared defaults for all external API circuit breakers
      default:
        register-health-indicator: true
        sliding-window-type: COUNT_BASED
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50          # Open at 50% failure rate
        slow-call-rate-threshold: 80        # Open if 80% of calls are slow
        slow-call-duration-threshold: 5s    # Calls >5s are "slow"
        wait-duration-in-open-state: 30s    # Wait 30s before half-open
        permitted-number-of-calls-in-half-open-state: 5
        automatic-transition-from-open-to-half-open-enabled: true
        record-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.client.HttpServerErrorException
        ignore-exceptions:
          - com.notifications.exception.RateLimitExceededException  # Handled by rate limiter
    instances:
      gmail:
        base-config: default
        slow-call-duration-threshold: 8s    # Gmail can be slower
        wait-duration-in-open-state: 60s    # Longer recovery — Gmail outages tend to be longer
      slack:
        base-config: default
        sliding-window-size: 15
        wait-duration-in-open-state: 20s    # Slack recovers quickly
      whatsapp:
        base-config: default
        wait-duration-in-open-state: 45s    # Twilio regional recovery
```
- **State transitions**: CLOSED → (≥50% failure rate in last 20 calls) → OPEN → (wait 30s) → HALF_OPEN → (5 test calls: if <50% fail → CLOSED, else → OPEN).
- **Monitoring**: Register health indicators with Spring Actuator. Expose circuit breaker state via `/actuator/health` and `/actuator/circuitbreakers`.
- **Fallback**: When circuit is OPEN, queue the notification for later delivery via a retry scheduler rather than dropping it.

---

## 5. Resilience4j Rate Limiter Configuration

**Decision**: Configure **per-provider outbound rate limiters** aligned to each API's documented limits, plus an **inbound rate limiter** for the service's own REST endpoints.

**Rationale**:
- **Respecting provider limits**: Each external API has specific rate limits. Proactively limiting outbound calls prevents 429 responses, which count against rate limits and waste quota. This is cheaper than reacting to 429s.
- **Inbound protection**: Rate limiting the service's REST API prevents abuse and ensures fair resource allocation across users.
- **Resilience4j native**: Using Resilience4j rate limiter (vs. Redis-based rate limiting) keeps the pattern consistent with circuit breakers and avoids additional infrastructure dependencies for this layer. Redis-based distributed rate limiting can be added later if horizontally scaled.

**Alternatives Considered**:
| Alternative | Why Not |
|---|---|
| **Redis-based rate limiting only** (e.g., Bucket4j + Redis) | Adds complexity. For a single-instance service, Resilience4j's in-memory rate limiter is sufficient and simpler. Can migrate to Redis-backed when scaling horizontally. |
| **Spring Cloud Gateway rate limiter** | Overkill for a single service. Gateway rate limiting is for API gateways fronting multiple services. |
| **No proactive rate limiting** (rely on 429 handling) | Wastes API quota, causes retries, and degrades user experience with delayed notifications. |

**Key Configuration/Notes**:
```yaml
resilience4j:
  ratelimiter:
    configs:
      default:
        register-health-indicator: true
        event-consumer-buffer-size: 100
    instances:
      # --- Outbound rate limiters (aligned to provider limits) ---
      gmail-api:
        limit-for-period: 200            # 200 quota units per period (conservative vs 250 limit)
        limit-refresh-period: 1s
        timeout-duration: 2s             # Wait up to 2s for a permit before failing
      slack-api:
        limit-for-period: 1              # 1 message per second per channel
        limit-refresh-period: 1s
        timeout-duration: 5s             # Queue briefly — Slack allows burst
      whatsapp-api:
        limit-for-period: 50             # 50 messages per period (conservative vs 80/s Twilio limit)
        limit-refresh-period: 1s
        timeout-duration: 3s
      # --- Inbound rate limiter (protect service API) ---
      inbound-api:
        limit-for-period: 100            # 100 requests per period
        limit-refresh-period: 1s
        timeout-duration: 0s             # Fail immediately if over limit (return 429)
```
- **Gmail quota mapping**: Each API call costs different quota units (messages.get=5, history.list=2). The rate limiter should acquire units proportional to the call cost. Implement a custom `RateLimiterAspect` that acquires N permits based on the Gmail API method.
- **Slack per-channel enforcement**: The Slack rate limiter should be keyed per channel ID, not globally. Use `RateLimiterRegistry` to dynamically create per-channel instances.
- **Inbound API**: Apply `@RateLimiter(name = "inbound-api")` to all controller methods. Return HTTP 429 with `Retry-After` header when exceeded.
- **Monitoring**: Expose rate limiter metrics via Micrometer → `/actuator/metrics/resilience4j.ratelimiter.*`.

---

## 6. OAuth2 Token Storage & Refresh

**Decision**: Store Gmail OAuth2 tokens (access + refresh) in **PostgreSQL encrypted at rest using AES-256-GCM** via a JPA `@Converter`, with **automatic proactive refresh** 5 minutes before expiry.

**Rationale**:
- **PostgreSQL over Redis**: Tokens are long-lived credentials tied to user accounts. They belong with user data in the relational database, not in an ephemeral cache. Redis TTL eviction could cause token loss.
- **AES-256-GCM encryption**: Provides both confidentiality and integrity (authenticated encryption). The encryption key is stored externally (environment variable or secrets manager — never in the database or source code).
- **JPA `@Converter`**: Transparently encrypts on write and decrypts on read. Application code works with plaintext tokens; encryption is a persistence concern only.
- **Proactive refresh**: Google OAuth2 access tokens expire in 1 hour. Refreshing 5 minutes before expiry avoids mid-request token failures. A scheduled task checks all tokens every minute and refreshes any expiring within 5 minutes.

**Alternatives Considered**:
| Alternative | Why Not |
|---|---|
| **Redis for token storage** | Volatile storage. TTL eviction or Redis restart loses tokens. Refresh tokens are long-lived and must be durable. |
| **HashiCorp Vault** | Excellent for secrets management but adds operational complexity (Vault cluster, unsealing, lease management). Overkill for MVP. Can adopt later. |
| **Spring Security OAuth2 Client** (in-memory token store) | Tokens lost on restart. No persistence. Only suitable for user-facing OAuth2 login flows, not server-side background token management. |
| **Database-level encryption** (PostgreSQL pgcrypto / TDE) | TDE encrypts the entire database — doesn't provide column-level granularity. pgcrypto requires raw SQL, breaking JPA abstraction. |

**Key Configuration/Notes**:
```java
// Token encryption converter
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    // AES-256-GCM with 12-byte IV, 128-bit auth tag
    // Key sourced from: ENCRYPTION_KEY environment variable (base64-encoded 256-bit key)
    // IV: randomly generated per encryption, prepended to ciphertext
    // Format stored in DB: base64(IV + ciphertext + authTag)
}
```
```yaml
oauth2:
  gmail:
    token-refresh:
      check-interval: PT1M              # Check every 1 minute
      refresh-before-expiry: PT5M       # Refresh 5 min before expiry
      max-refresh-retries: 3            # Retry refresh 3 times before marking errored
      retry-delay: PT10S                # 10s between retries
    encryption:
      algorithm: AES/GCM/NoPadding
      key-size: 256
      key-source: ENV                   # ENCRYPTION_KEY env variable
```
- **Token lifecycle**: `authorization_code` → exchange for `access_token` + `refresh_token` → encrypt both → store in `email_accounts` table → scheduled refresh task → on refresh failure after 3 retries, set `EmailAccount.status = ERROR` and notify user.
- **Refresh token rotation**: Google may rotate refresh tokens. Always persist the latest refresh token returned from a token refresh response.
- **Key rotation**: Support key versioning. Store a `key_version` column alongside encrypted tokens. On key rotation, re-encrypt all tokens in a background migration.

---

## 7. Email Deduplication Strategy

**Decision**: Use **Redis SET with TTL** (`SADD` / `SISMEMBER`) keyed per user, with a **48-hour TTL** on the deduplication set.

**Rationale**:
- **Simplicity**: Redis `SET` with `SISMEMBER` is O(1) for lookups and O(1) for inserts. No false positives (unlike Bloom filters). Deterministic and debuggable.
- **Per-user isolation**: Key pattern `dedup:{userId}` keeps each user's deduplication set independent. No cross-user interference.
- **48-hour TTL**: Gmail's `history.list` may redeliver message IDs if `historyId` is replayed (e.g., after a Pub/Sub redelivery or fallback poll). 48 hours covers the maximum realistic redelivery window while keeping memory usage bounded.
- **Memory efficiency**: Each Gmail message ID is ~16 characters. For 1,000 users × 200 emails/day × 48h = ~400K entries × ~50 bytes = ~20 MB total Redis memory. Negligible.

**Alternatives Considered**:
| Alternative | Why Not |
|---|---|
| **Redis Bloom Filter** (`BF.ADD` / `BF.EXISTS`) | False positive rate (even at 1%) means some legitimate emails would be silently dropped. Unacceptable for a notification service where missed notifications are a critical failure. |
| **Redis Sorted Set** (with timestamp score) | Unnecessary complexity. Sorted sets are useful when you need ordering or range queries on dedup entries. We only need exists/not-exists. Higher memory overhead per entry. |
| **PostgreSQL unique constraint** | Adds write contention to the primary database on every email scan. Redis is faster for high-frequency idempotency checks. |
| **In-memory `ConcurrentHashMap`** | Lost on restart. Not shared across instances if horizontally scaled. |

**Key Configuration/Notes**:
```yaml
deduplication:
  redis:
    key-prefix: "dedup"
    key-pattern: "dedup:{userId}"       # One SET per user
    ttl: PT48H                          # 48-hour expiry
    # Member value: Gmail message ID (globally unique per mailbox)
```
```java
// Deduplication check (pseudo-code)
public boolean isDuplicate(String userId, String messageId) {
    String key = "dedup:" + userId;
    // SISMEMBER returns true if already processed
    Boolean exists = redisTemplate.opsForSet().isMember(key, messageId);
    if (Boolean.TRUE.equals(exists)) {
        return true;
    }
    // SADD + set TTL only on first add (key creation)
    redisTemplate.opsForSet().add(key, messageId);
    redisTemplate.expire(key, Duration.ofHours(48));
    return false;
}
```
- **Race condition mitigation**: Use a Lua script to atomically check-and-add: `if redis.call('SISMEMBER', key, id) == 1 then return 1 else redis.call('SADD', key, id); redis.call('EXPIRE', key, ttl); return 0 end`. This prevents two concurrent scan threads from both passing the check.
- **Cleanup**: TTL handles expiry automatically. No manual cleanup needed.
- **Observability**: Monitor Redis memory usage and SET cardinality via `SCARD dedup:{userId}` for anomaly detection (e.g., a user suddenly receiving 10,000 emails may indicate spam).

---

## 8. Notification Batching

**Decision**: Use a **time-window aggregation pattern** with a **1-minute tumbling window** implemented via a **per-user in-memory buffer** flushed by a scheduled task and a count threshold trigger.

**Rationale**:
- **Spec requirement**: Batch when >10 emails arrive in a 1-minute window. This naturally maps to a tumbling window with a count threshold.
- **Dual-trigger flush**: The buffer flushes when either (a) the 1-minute window expires (time trigger) OR (b) the count reaches 10 and continues accumulating until the window closes (count trigger switches mode from individual to batch). This ensures low-latency delivery for normal traffic while batching during bursts.
- **In-memory buffer**: For a single-instance service, a `ConcurrentHashMap<String, BatchWindow>` per user is simple and fast. No external infrastructure needed.
- **Summary notification**: When batched, send a single notification: `"📧 You received {count} new emails in the last minute"` with a list of senders/subjects (up to 10 shown, rest summarized).

**Alternatives Considered**:
| Alternative | Why Not |
|---|---|
| **Redis-based windowing** (Sorted Set with timestamp scores) | Necessary for horizontal scaling but adds complexity and latency for MVP. Can migrate to Redis-based windows when scaling out. |
| **Spring Integration / Spring Cloud Stream** (windowing DSL) | Heavy framework dependency for a simple aggregation pattern. Adds configuration complexity without proportional benefit. |
| **Kafka Streams / Flink** | Massively over-engineered for this use case. Designed for high-throughput stream processing across distributed systems. |
| **No batching** (always send individual) | Per the spec, >10 emails/minute should be batched. Individual notifications during a burst create noise and may hit Slack/WhatsApp rate limits. |

**Key Configuration/Notes**:
```yaml
notification:
  batching:
    window-duration: PT1M               # 1-minute tumbling window
    batch-threshold: 10                  # Switch to batch mode at 10 emails
    max-batch-size: 100                  # Safety cap per window
    summary-display-limit: 10           # Show top 10 senders/subjects in summary
    flush-scheduler-interval: PT10S     # Check for expired windows every 10s
```
```java
// Batching logic (conceptual)
public class NotificationBatcher {
    // Per-user batch windows
    private final ConcurrentHashMap<String, BatchWindow> windows = new ConcurrentHashMap<>();

    public void addEmail(String userId, EmailSummary email) {
        BatchWindow window = windows.computeIfAbsent(userId, 
            k -> new BatchWindow(Instant.now()));
        window.add(email);
        
        // If first email in window and below threshold, send immediately
        if (window.count() == 1) {
            scheduleWindowFlush(userId, Duration.ofMinutes(1));
        }
        // Individual emails sent immediately if count < threshold
        // Once threshold reached, hold remaining for batch summary
    }

    // Scheduled flush: if window has >threshold emails, send batch summary
    // If ≤threshold, all were already sent individually — no action needed
    @Scheduled(fixedRate = 10_000)  // Every 10 seconds
    public void flushExpiredWindows() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(1));
        windows.forEach((userId, window) -> {
            if (window.startedBefore(cutoff)) {
                if (window.count() > batchThreshold) {
                    sendBatchSummary(userId, window);
                }
                windows.remove(userId);
            }
        });
    }
}
```
- **Behavior**:
  - Emails 1–10: Sent individually in real-time (no delay).
  - Email 11+: Held in buffer. When the 1-minute window closes, a single batch summary is sent covering emails 11+.
  - This hybrid approach ensures users get fast delivery for normal volume and a clean summary during bursts.
- **Horizontal scaling note**: When scaling to multiple instances, replace the `ConcurrentHashMap` with a Redis-backed window (Sorted Set with message IDs scored by timestamp). Use a distributed lock (Redisson) for flush coordination.

---

## Summary Matrix

| Topic | Decision | Key Dependency |
|---|---|---|
| Gmail Integration | Gmail API + Cloud Pub/Sub push | `google-api-services-gmail`, `spring-cloud-gcp-starter-pubsub` |
| Slack Integration | Slack Web API via official SDK | `com.slack.api:slack-api-client` |
| WhatsApp Integration | Twilio WhatsApp API | `com.twilio.sdk:twilio` |
| Circuit Breaker | Per-provider, count-based, 50% failure threshold | `resilience4j-spring-boot3` |
| Rate Limiter | Per-provider outbound + inbound, aligned to API limits | `resilience4j-spring-boot3` |
| OAuth2 Tokens | PostgreSQL + AES-256-GCM encryption + proactive refresh | JPA `@Converter`, `google-auth-library-oauth2-http` |
| Deduplication | Redis SET with 48h TTL, atomic Lua check-and-add | `spring-boot-starter-data-redis` |
| Batching | 1-min tumbling window, in-memory buffer, dual-trigger flush | `ConcurrentHashMap` + `@Scheduled` |
