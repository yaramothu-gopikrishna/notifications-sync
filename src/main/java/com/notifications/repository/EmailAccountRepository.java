package com.notifications.repository;

import com.notifications.domain.EmailAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailAccountRepository extends JpaRepository<EmailAccount, UUID> {
    List<EmailAccount> findByUserId(UUID userId);
    List<EmailAccount> findByUserIdAndStatus(UUID userId, String status);
    List<EmailAccount> findByStatus(String status);
    Optional<EmailAccount> findByIdAndUserId(UUID id, UUID userId);
    @Query("SELECT ea FROM EmailAccount ea JOIN FETCH ea.user WHERE ea.status = :status")
    List<EmailAccount> findByStatusWithUser(@Param("status") String status);
}
