package com.notifications.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String email;
    private boolean notificationsPaused;
    private Instant createdAt;
    private String gravatarUrl;
}
