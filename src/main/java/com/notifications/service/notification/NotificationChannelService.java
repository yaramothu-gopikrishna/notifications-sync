package com.notifications.service.notification;

import com.notifications.dto.request.NotificationChannelRequest;
import com.notifications.dto.response.NotificationChannelResponse;
import java.util.List;
import java.util.UUID;

public interface NotificationChannelService {
    NotificationChannelResponse createChannel(UUID userId, NotificationChannelRequest request);
    List<NotificationChannelResponse> listChannels(UUID userId);
    NotificationChannelResponse updateChannel(UUID userId, UUID channelId, NotificationChannelRequest request);
    void deleteChannel(UUID userId, UUID channelId);
}
