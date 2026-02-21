package com.notifications.service.email;

import com.google.api.services.gmail.Gmail;
import com.notifications.domain.EmailAccount;
import com.notifications.domain.User;
import com.notifications.dto.response.EmailAccountResponse;
import com.notifications.exception.EmailConnectionException;
import com.notifications.exception.ResourceNotFoundException;
import com.notifications.mapper.EmailAccountMapper;
import com.notifications.repository.EmailAccountRepository;
import com.notifications.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAccountServiceImpl implements EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;
    private final GmailClientService gmailClientService;
    private final EmailAccountMapper emailAccountMapper;

    @Override
    public String initiateConnection(UUID userId) {
        return gmailClientService.buildAuthorizationUrl(userId);
    }

    @Override
    @Transactional
    public EmailAccountResponse completeConnection(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Map<String, String> tokens = gmailClientService.exchangeCode(code);

        EmailAccount account = new EmailAccount();
        account.setUser(user);
        account.setProvider("gmail");
        account.setAccessToken(tokens.get("accessToken"));
        account.setRefreshToken(tokens.get("refreshToken"));
        account.setStatus("active");

        long expiresIn = Long.parseLong(tokens.get("expiresInSeconds"));
        account.setTokenExpiresAt(Instant.now().plusSeconds(expiresIn));

        // Fetch email address and initial historyId
        try {
            Gmail gmail = gmailClientService.getGmailService(tokens.get("accessToken"));
            var profile = gmail.users().getProfile("me").execute();
            account.setEmailAddress(profile.getEmailAddress());
            account.setHistoryId(profile.getHistoryId().toString());
        } catch (Exception e) {
            throw new EmailConnectionException("Failed to fetch Gmail profile", e);
        }

        account = emailAccountRepository.save(account);
        log.info("Email account connected: {} for user {}", account.getEmailAddress(), userId);
        return emailAccountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailAccountResponse> listAccounts(UUID userId) {
        return emailAccountRepository.findByUserId(userId).stream()
                .map(emailAccountMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailAccountResponse getAccount(UUID userId, UUID accountId) {
        EmailAccount account = emailAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailAccount", accountId));
        return emailAccountMapper.toResponse(account);
    }

    @Override
    @Transactional
    public void disconnectAccount(UUID userId, UUID accountId) {
        EmailAccount account = emailAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailAccount", accountId));
        emailAccountRepository.delete(account);
        log.info("Email account disconnected: {} for user {}", account.getEmailAddress(), userId);
    }

    @Override
    @Transactional
    public EmailAccountResponse pauseAccount(UUID userId, UUID accountId) {
        EmailAccount account = emailAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailAccount", accountId));
        if (!"active".equals(account.getStatus())) {
            throw new IllegalStateException("Can only pause active accounts, current status: " + account.getStatus());
        }
        account.setStatus("paused");
        account = emailAccountRepository.save(account);
        log.info("Email account paused: {} for user {}", account.getEmailAddress(), userId);
        return emailAccountMapper.toResponse(account);
    }

    @Override
    @Transactional
    public EmailAccountResponse resumeAccount(UUID userId, UUID accountId) {
        EmailAccount account = emailAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailAccount", accountId));
        if (!"paused".equals(account.getStatus())) {
            throw new IllegalStateException("Can only resume paused accounts, current status: " + account.getStatus());
        }
        account.setStatus("active");
        account = emailAccountRepository.save(account);
        log.info("Email account resumed: {} for user {}", account.getEmailAddress(), userId);
        return emailAccountMapper.toResponse(account);
    }
}
