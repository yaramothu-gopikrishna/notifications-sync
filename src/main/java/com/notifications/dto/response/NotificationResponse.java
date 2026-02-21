package com.notifications.dto.response;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class NotificationResponse {
    private UUID id;
    private String senderName;
    private String senderAddress;
    private String subject;
    private String preview;
    private String deliveryStatus;
    private String channelType;
    private Instant emailReceivedAt;
    private Instant deliveredAt;
    private Instant createdAt;
}
