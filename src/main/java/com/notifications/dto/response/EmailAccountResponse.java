package com.notifications.dto.response;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class EmailAccountResponse {
    private UUID id;
    private String provider;
    private String emailAddress;
    private String status;
    private Instant lastScannedAt;
    private Instant createdAt;
}
