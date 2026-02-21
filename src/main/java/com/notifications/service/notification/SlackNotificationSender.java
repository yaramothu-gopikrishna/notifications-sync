package com.notifications.service.notification;

import com.notifications.domain.NotificationChannel;
import com.notifications.exception.NotificationDeliveryException;
import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SlackNotificationSender implements NotificationSender {

    private final Slack slack = Slack.getInstance();

    @Override
    @CircuitBreaker(name = "slack", fallbackMethod = "slackFallback")
    @RateLimiter(name = "slack-api")
    public String send(NotificationChannel channel, String senderName, String subject, String preview) {
        try {
            String message = String.format("📧 *New email from %s*\n*Subject:* %s\n%s",
                    senderName, subject, truncatePreview(preview));

            ChatPostMessageResponse response = slack.methods(channel.getBotToken())
                    .chatPostMessage(req -> req
                            .channel(channel.getSlackChannelId())
                            .text(message)
                            .mrkdwn(true));

            if (!response.isOk()) {
                throw new NotificationDeliveryException("Slack API error: " + response.getError());
            }

            log.info("Slack notification sent to channel {}", channel.getSlackChannelId());
            return response.getTs();
        } catch (NotificationDeliveryException e) {
            throw e;
        } catch (Exception e) {
            throw new NotificationDeliveryException("Failed to send Slack notification", e);
        }
    }

    @Override
    public boolean supports(String channelType) {
        return "slack".equalsIgnoreCase(channelType);
    }

    @SuppressWarnings("unused")
    private String slackFallback(NotificationChannel channel, String senderName,
                                  String subject, String preview, Throwable t) {
        log.warn("Slack circuit breaker open, notification queued for retry: {}", t.getMessage());
        throw new NotificationDeliveryException("Slack service unavailable", t);
    }

    private String truncatePreview(String preview) {
        if (preview == null) return "";
        return preview.length() > 150 ? preview.substring(0, 150) + "…" : preview;
    }
}
