package com.notifications.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_account_id")
    private EmailAccount emailAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_channel_id")
    private NotificationChannel notificationChannel;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_address", length = 320)
    private String senderAddress;

    @Column(length = 998)
    private String subject;

    @Column(length = 200)
    private String preview;

    @Column(name = "delivery_status", nullable = false, length = 20)
    private String deliveryStatus = "pending";

    @Column(name = "external_message_id")
    private String externalMessageId;

    @Column(name = "retry_count", nullable = false)
    private short retryCount = 0;

    @Column(name = "email_received_at")
    private Instant emailReceivedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
