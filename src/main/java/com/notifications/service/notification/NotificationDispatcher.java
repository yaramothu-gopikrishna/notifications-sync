package com.notifications.service.notification;

import com.notifications.domain.*;
import com.notifications.repository.NotificationChannelRepository;
import com.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final NotificationChannelRepository channelRepository;
    private final NotificationRepository notificationRepository;
    private final List<NotificationSender> senders;

    private final ConcurrentHashMap<UUID, BatchWindow> batchWindows = new ConcurrentHashMap<>();

    @Transactional
    public void dispatch(User user, EmailAccount account,
                         String senderName, String senderAddress,
                         String subject, String preview, Instant emailReceivedAt) {

        List<NotificationChannel> channels = channelRepository
                .findByUserIdAndStatus(user.getId(), "active");

        if (channels.isEmpty()) {
            log.warn("No active notification channels for user {}", user.getId());
            return;
        }

        // Check batching threshold
        BatchWindow window = batchWindows.computeIfAbsent(user.getId(),
                k -> new BatchWindow(Instant.now()));

        window.incrementCount();

        if (window.getCount() > 10) {
            // Buffer for batch summary — create notification record as "batched"
            for (NotificationChannel channel : channels) {
                createNotification(user, account, channel, senderName, senderAddress,
                        subject, preview, emailReceivedAt, "batched", null);
            }
            return;
        }

        // Send individually
        for (NotificationChannel channel : channels) {
            sendToChannel(user, account, channel, senderName, senderAddress,
                    subject, preview, emailReceivedAt);
        }
    }

    private void sendToChannel(User user, EmailAccount account, NotificationChannel channel,
                                String senderName, String senderAddress,
                                String subject, String preview, Instant emailReceivedAt) {
        NotificationSender sender = senders.stream()
                .filter(s -> s.supports(channel.getChannelType()))
                .findFirst()
                .orElse(null);

        if (sender == null) {
            log.error("No sender found for channel type: {}", channel.getChannelType());
            return;
        }

        try {
            String externalId = sender.send(channel, senderName, subject, preview);
            createNotification(user, account, channel, senderName, senderAddress,
                    subject, preview, emailReceivedAt, "sent", externalId);
        } catch (Exception e) {
            log.error("Failed to send notification via {}: {}", channel.getChannelType(), e.getMessage());
            createNotification(user, account, channel, senderName, senderAddress,
                    subject, preview, emailReceivedAt, "failed", null);
        }
    }

    private Notification createNotification(User user, EmailAccount account,
                                             NotificationChannel channel,
                                             String senderName, String senderAddress,
                                             String subject, String preview,
                                             Instant emailReceivedAt,
                                             String status, String externalId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEmailAccount(account);
        notification.setNotificationChannel(channel);
        notification.setSenderName(senderName);
        notification.setSenderAddress(senderAddress);
        notification.setSubject(subject);
        notification.setPreview(preview != null && preview.length() > 200 ? preview.substring(0, 200) : preview);
        notification.setEmailReceivedAt(emailReceivedAt);
        notification.setDeliveryStatus(status);
        notification.setExternalMessageId(externalId);
        if ("sent".equals(status)) {
            notification.setDeliveredAt(Instant.now());
        }
        return notificationRepository.save(notification);
    }

    // Called by scheduler to flush expired batch windows
    public void flushBatchWindows() {
        Instant cutoff = Instant.now().minusSeconds(60);
        batchWindows.forEach((userId, window) -> {
            if (window.getStartTime().isBefore(cutoff) && window.getCount() > 10) {
                log.info("Flushing batch window for user {}: {} emails", userId, window.getCount());
                // Batch summary sending is handled by the retry scheduler
                // picking up "batched" status notifications
            }
            if (window.getStartTime().isBefore(cutoff)) {
                batchWindows.remove(userId);
            }
        });
    }

    // Simple inner class for batch window tracking
    static class BatchWindow {
        private final Instant startTime;
        private int count;

        BatchWindow(Instant startTime) {
            this.startTime = startTime;
            this.count = 0;
        }

        Instant getStartTime() { return startTime; }
        int getCount() { return count; }
        void incrementCount() { count++; }
    }
}
