package com.notifications.mapper;

import com.notifications.domain.Notification;
import com.notifications.domain.NotificationChannel;
import com.notifications.dto.response.NotificationResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-21T12:06:09+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponse toResponse(Notification entity) {
        if ( entity == null ) {
            return null;
        }

        NotificationResponse notificationResponse = new NotificationResponse();

        notificationResponse.setChannelType( entityNotificationChannelChannelType( entity ) );
        notificationResponse.setId( entity.getId() );
        notificationResponse.setSenderName( entity.getSenderName() );
        notificationResponse.setSenderAddress( entity.getSenderAddress() );
        notificationResponse.setSubject( entity.getSubject() );
        notificationResponse.setPreview( entity.getPreview() );
        notificationResponse.setDeliveryStatus( entity.getDeliveryStatus() );
        notificationResponse.setEmailReceivedAt( entity.getEmailReceivedAt() );
        notificationResponse.setDeliveredAt( entity.getDeliveredAt() );
        notificationResponse.setCreatedAt( entity.getCreatedAt() );

        return notificationResponse;
    }

    private String entityNotificationChannelChannelType(Notification notification) {
        if ( notification == null ) {
            return null;
        }
        NotificationChannel notificationChannel = notification.getNotificationChannel();
        if ( notificationChannel == null ) {
            return null;
        }
        String channelType = notificationChannel.getChannelType();
        if ( channelType == null ) {
            return null;
        }
        return channelType;
    }
}
