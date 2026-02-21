package com.notifications.repository;

import com.notifications.domain.EmailAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailAccountRepository extends JpaRepository<EmailAccount, UUID> {
    List<EmailAccount> findByUserId(UUID userId);
    List<EmailAccount> findByUserIdAndStatus(UUID userId, String status);
    List<EmailAccount> findByStatus(String status);
    Optional<EmailAccount> findByIdAndUserId(UUID id, UUID userId);
}
