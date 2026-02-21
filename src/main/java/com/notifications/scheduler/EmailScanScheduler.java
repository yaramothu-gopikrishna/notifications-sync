package com.notifications.scheduler;

import com.notifications.domain.EmailAccount;
import com.notifications.repository.EmailAccountRepository;
import com.notifications.service.email.EmailScannerService;
import com.notifications.service.notification.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailScanScheduler {

    private final EmailAccountRepository emailAccountRepository;
    private final EmailScannerService emailScannerService;
    private final NotificationDispatcher notificationDispatcher;

    @Scheduled(fixedDelayString = "${gmail.scan.interval:60000}")
    public void scanAllActiveAccounts() {
        List<EmailAccount> activeAccounts = emailAccountRepository.findByStatusWithUser("active");
        log.debug("Scanning {} active email accounts", activeAccounts.size());

        for (EmailAccount account : activeAccounts) {
            if (account.getUser().isNotificationsPaused()) {
                continue;
            }
            try {
                emailScannerService.scanAccount(account);
            } catch (Exception e) {
                log.error("Error scanning account {}: {}",
                        account.getEmailAddress(), e.getMessage());
            }
        }

        // Flush batch windows
        notificationDispatcher.flushBatchWindows();
    }
}
