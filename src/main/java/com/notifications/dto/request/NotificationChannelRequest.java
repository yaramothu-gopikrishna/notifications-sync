package com.notifications.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationChannelRequest {
    @NotBlank
    private String channelType; // slack or whatsapp

    private String botToken;
    private String slackChannelId;
    private String whatsappPhoneNumber;
    private String twilioSid;
    private boolean consentGiven;
}
