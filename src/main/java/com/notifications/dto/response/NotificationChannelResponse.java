package com.notifications.dto.response;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class NotificationChannelResponse {
    private UUID id;
    private String channelType;
    private String status;
    private String slackChannelId;
    private String whatsappPhoneNumber;
    private boolean consentGiven;
    private Instant createdAt;
}
