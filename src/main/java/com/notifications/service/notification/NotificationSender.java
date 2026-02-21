package com.notifications.service.notification;

import com.notifications.domain.NotificationChannel;

public interface NotificationSender {
    String send(NotificationChannel channel, String senderName, String subject, String preview);
    boolean supports(String channelType);
}
