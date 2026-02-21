package com.notifications.repository;

import com.notifications.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Notification> findByDeliveryStatusAndRetryCountLessThan(String status, short maxRetries);

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.notificationChannel WHERE n.deliveryStatus = :status AND n.retryCount < :maxRetries")
    List<Notification> findFailedWithChannel(@Param("status") String status, @Param("maxRetries") short maxRetries);
}
