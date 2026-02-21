package com.notifications.service.notification;

import com.notifications.domain.NotificationChannel;
import com.notifications.exception.NotificationDeliveryException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WhatsAppNotificationSender implements NotificationSender {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.whatsapp.from-number:whatsapp:+14155238886}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank()) {
            Twilio.init(accountSid, authToken);
        }
    }

    @Override
    @CircuitBreaker(name = "whatsapp", fallbackMethod = "whatsappFallback")
    @RateLimiter(name = "whatsapp-api")
    public String send(NotificationChannel channel, String senderName, String subject, String preview) {
        try {
            String body = String.format("📧 New email from %s\nSubject: %s\n%s",
                    senderName, subject, truncatePreview(preview));

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + channel.getWhatsappPhoneNumber()),
                    new PhoneNumber(fromNumber),
                    body
            ).create();

            log.info("WhatsApp notification sent to {}, SID: {}",
                    channel.getWhatsappPhoneNumber(), message.getSid());
            return message.getSid();
        } catch (Exception e) {
            throw new NotificationDeliveryException("Failed to send WhatsApp notification", e);
        }
    }

    @Override
    public boolean supports(String channelType) {
        return "whatsapp".equalsIgnoreCase(channelType);
    }

    @SuppressWarnings("unused")
    private String whatsappFallback(NotificationChannel channel, String senderName,
                                     String subject, String preview, Throwable t) {
        log.warn("WhatsApp circuit breaker open, notification queued for retry: {}", t.getMessage());
        throw new NotificationDeliveryException("WhatsApp service unavailable", t);
    }

    private String truncatePreview(String preview) {
        if (preview == null) return "";
        return preview.length() > 150 ? preview.substring(0, 150) + "…" : preview;
    }
}
