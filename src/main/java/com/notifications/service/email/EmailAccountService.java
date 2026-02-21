package com.notifications.service.email;

import com.notifications.dto.response.EmailAccountResponse;
import java.util.List;
import java.util.UUID;

public interface EmailAccountService {
    String initiateConnection(UUID userId);
    EmailAccountResponse completeConnection(UUID userId, String code);
    List<EmailAccountResponse> listAccounts(UUID userId);
    EmailAccountResponse getAccount(UUID userId, UUID accountId);
    void disconnectAccount(UUID userId, UUID accountId);
    EmailAccountResponse pauseAccount(UUID userId, UUID accountId);
    EmailAccountResponse resumeAccount(UUID userId, UUID accountId);
}
