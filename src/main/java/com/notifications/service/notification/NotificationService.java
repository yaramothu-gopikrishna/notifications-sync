package com.notifications.service.notification;

import com.notifications.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface NotificationService {
    Page<NotificationResponse> getNotificationHistory(UUID userId, Pageable pageable);
}
