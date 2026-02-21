package com.notifications.service.email;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.notifications.domain.EmailAccount;
import com.notifications.repository.EmailAccountRepository;
import com.notifications.service.dedup.DeduplicationService;
import com.notifications.service.filter.FilterService;
import com.notifications.service.notification.NotificationDispatcher;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailScannerServiceImpl implements EmailScannerService {

    private final GmailClientService gmailClientService;
    private final DeduplicationService deduplicationService;
    private final NotificationDispatcher notificationDispatcher;
    private final EmailAccountRepository emailAccountRepository;
    private final FilterService filterService;

    @Override
    @CircuitBreaker(name = "gmail", fallbackMethod = "scanFallback")
    @RateLimiter(name = "gmail-api")
    public void scanAccount(EmailAccount account) {
        log.debug("Scanning email account: {}", account.getEmailAddress());

        try {
            Gmail gmail = gmailClientService.getGmailService(account.getAccessToken());
            String historyId = account.getHistoryId();

            if (historyId == null) {
                // First scan — get current history ID as baseline
                var profile = gmail.users().getProfile("me").execute();
                account.setHistoryId(profile.getHistoryId().toString());
                account.setLastScannedAt(Instant.now());
                emailAccountRepository.save(account);
                return;
            }

            ListHistoryResponse response = gmail.users().history().list("me")
                    .setStartHistoryId(new BigInteger(historyId))
                    .setHistoryTypes(List.of("messageAdded"))
                    .setMaxResults(100L)
                    .execute();

            if (response.getHistory() == null) {
                account.setLastScannedAt(Instant.now());
                emailAccountRepository.save(account);
                return;
            }

            for (History history : response.getHistory()) {
                if (history.getMessagesAdded() == null) continue;
                for (var added : history.getMessagesAdded()) {
                    processMessage(gmail, account, added.getMessage().getId());
                }
            }

            // Update historyId to latest
            if (response.getHistoryId() != null) {
                account.setHistoryId(response.getHistoryId().toString());
            }
            account.setLastScannedAt(Instant.now());
            emailAccountRepository.save(account);

        } catch (Exception e) {
            log.error("Error scanning account {}: {}", account.getEmailAddress(), e.getMessage());
            handleScanError(account);
        }
    }

    private void processMessage(Gmail gmail, EmailAccount account, String messageId) {
        UUID userId = account.getUser().getId();

        if (deduplicationService.isDuplicate(userId, messageId)) {
            return;
        }

        try {
            Message message = gmail.users().messages().get("me", messageId)
                    .setFormat("metadata")
                    .setMetadataHeaders(List.of("From", "Subject"))
                    .execute();

            // Skip already-read messages
            if (message.getLabelIds() != null && !message.getLabelIds().contains("UNREAD")) {
                deduplicationService.markProcessed(userId, messageId);
                return;
            }

            String senderFull = getHeader(message, "From");
            String subject = getHeader(message, "Subject");
            String senderName = extractName(senderFull);
            String senderAddress = extractEmail(senderFull);
            String preview = message.getSnippet();
            if (preview != null && preview.length() > 150) {
                preview = preview.substring(0, 150) + "…";
            }

            // Apply filters
            if (!filterService.shouldNotify(userId, senderAddress, subject)) {
                deduplicationService.markProcessed(userId, messageId);
                return;
            }

            notificationDispatcher.dispatch(account.getUser(), account,
                    senderName, senderAddress, subject, preview,
                    Instant.ofEpochMilli(message.getInternalDate()));

            deduplicationService.markProcessed(userId, messageId);

        } catch (Exception e) {
            log.error("Error processing message {}: {}", messageId, e.getMessage());
        }
    }

    private String getHeader(Message message, String name) {
        if (message.getPayload() == null || message.getPayload().getHeaders() == null) return "";
        return message.getPayload().getHeaders().stream()
                .filter(h -> name.equalsIgnoreCase(h.getName()))
                .map(MessagePartHeader::getValue)
                .findFirst().orElse("");
    }

    private String extractName(String from) {
        if (from.contains("<")) return from.substring(0, from.indexOf("<")).trim().replace("\"", "");
        return from;
    }

    private String extractEmail(String from) {
        if (from.contains("<") && from.contains(">")) {
            return from.substring(from.indexOf("<") + 1, from.indexOf(">"));
        }
        return from;
    }

    private void handleScanError(EmailAccount account) {
        // After persistent failures, mark account as error
        account.setStatus("error");
        account.setLastScannedAt(Instant.now());
        emailAccountRepository.save(account);
        log.warn("Email account {} marked as error", account.getEmailAddress());
    }

    @SuppressWarnings("unused")
    private void scanFallback(EmailAccount account, Throwable t) {
        log.warn("Gmail circuit breaker open for account {}: {}",
                account.getEmailAddress(), t.getMessage());
    }
}
