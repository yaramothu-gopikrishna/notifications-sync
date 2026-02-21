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
@Table(name = "email_accounts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "email_address"}))
@Getter @Setter @NoArgsConstructor
public class EmailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String provider = "gmail";

    @Column(name = "email_address", nullable = false, length = 320)
    private String emailAddress;

    @Column(nullable = false, length = 20)
    private String status = "active";

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "access_token_encrypted", columnDefinition = "TEXT")
    private String accessToken;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "refresh_token_encrypted", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "key_version", nullable = false)
    private short keyVersion = 1;

    @Column(name = "history_id")
    private String historyId;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "last_scanned_at")
    private Instant lastScannedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
