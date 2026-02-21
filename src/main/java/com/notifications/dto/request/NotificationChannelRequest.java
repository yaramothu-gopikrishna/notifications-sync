package com.notifications.dto.request;

import lombok.Data;

@Data
public class NotificationChannelRequest {
    private String channelType; // slack or whatsapp

    private String botToken;
    private String slackChannelId;
    private String whatsappPhoneNumber;
    private String twilioSid;
    private Boolean consentGiven;
}
