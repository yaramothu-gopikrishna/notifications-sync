package com.notifications.service.dedup;

import java.util.UUID;

public interface DeduplicationService {
    boolean isDuplicate(UUID userId, String gmailMessageId);
    void markProcessed(UUID userId, String gmailMessageId);
}
