package com.notifications.mapper;

import com.notifications.domain.NotificationChannel;
import com.notifications.dto.request.NotificationChannelRequest;
import com.notifications.dto.response.NotificationChannelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationChannelMapper {

    NotificationChannelResponse toResponse(NotificationChannel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "keyVersion", ignore = true)
    @Mapping(target = "consentGivenAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NotificationChannel toEntity(NotificationChannelRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "keyVersion", ignore = true)
    @Mapping(target = "consentGivenAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(NotificationChannelRequest request, @MappingTarget NotificationChannel entity);
}
