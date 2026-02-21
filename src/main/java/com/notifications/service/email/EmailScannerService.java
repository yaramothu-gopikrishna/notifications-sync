package com.notifications.service.email;

import com.notifications.domain.EmailAccount;

public interface EmailScannerService {
    void scanAccount(EmailAccount account);
}
