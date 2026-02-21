package com.notifications.domain;

import com.notifications.config.EncryptionConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_channels",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_type"}))
@Getter @Setter @NoArgsConstructor
public class NotificationChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "channel_type", nullable = false, length = 20)
    private String channelType;

    @Column(nullable = false, length = 20)
    private String status = "active";

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "bot_token_encrypted", columnDefinition = "TEXT")
    private String botToken;

    @Column(name = "slack_channel_id")
    private String slackChannelId;

    @Column(name = "whatsapp_phone_number", length = 20)
    private String whatsappPhoneNumber;

    @Column(name = "twilio_sid")
    private String twilioSid;

    @Column(name = "key_version", nullable = false)
    private short keyVersion = 1;

    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven = false;

    @Column(name = "consent_given_at")
    private Instant consentGivenAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
