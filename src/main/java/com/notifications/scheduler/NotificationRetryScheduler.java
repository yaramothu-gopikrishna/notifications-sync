package com.notifications.scheduler;

import com.notifications.domain.Notification;
import com.notifications.domain.NotificationChannel;
import com.notifications.repository.NotificationRepository;
import com.notifications.service.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationRepository notificationRepository;
    private final List<NotificationSender> senders;

    @Scheduled(fixedDelay = 30000)
    public void retryFailedNotifications() {
        List<Notification> failed = notificationRepository
                .findByDeliveryStatusAndRetryCountLessThan("failed", (short) 3);

        for (Notification notification : failed) {
            // Exponential backoff: only retry if enough time has passed
            long backoffSeconds = 30L * (1L << notification.getRetryCount());
            if (notification.getUpdatedAt().plusSeconds(backoffSeconds).isAfter(Instant.now())) {
                continue;
            }

            NotificationChannel channel = notification.getNotificationChannel();
            if (channel == null || !"active".equals(channel.getStatus())) {
                continue;
            }

            NotificationSender sender = senders.stream()
                    .filter(s -> s.supports(channel.getChannelType()))
                    .findFirst().orElse(null);

            if (sender == null) continue;

            try {
                String externalId = sender.send(channel,
                        notification.getSenderName(),
                        notification.getSubject(),
                        notification.getPreview());
                notification.setDeliveryStatus("sent");
                notification.setExternalMessageId(externalId);
                notification.setDeliveredAt(Instant.now());
                log.info("Retry succeeded for notification {}", notification.getId());
            } catch (Exception e) {
                notification.setRetryCount((short) (notification.getRetryCount() + 1));
                log.warn("Retry failed for notification {} (attempt {}): {}",
                        notification.getId(), notification.getRetryCount(), e.getMessage());
            }
            notificationRepository.save(notification);
        }
    }
}
