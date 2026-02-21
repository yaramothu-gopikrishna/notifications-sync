package com.notifications.repository;

import com.notifications.domain.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, UUID> {
    List<NotificationChannel> findByUserId(UUID userId);
    List<NotificationChannel> findByUserIdAndStatus(UUID userId, String status);
    Optional<NotificationChannel> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByUserIdAndChannelType(UUID userId, String channelType);
}
