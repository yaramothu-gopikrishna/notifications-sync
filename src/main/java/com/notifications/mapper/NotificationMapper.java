package com.notifications.mapper;

import com.notifications.domain.Notification;
import com.notifications.dto.response.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "notificationChannel.channelType", target = "channelType")
    NotificationResponse toResponse(Notification entity);
}
