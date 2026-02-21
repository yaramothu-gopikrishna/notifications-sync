package com.notifications.mapper;

import com.notifications.domain.NotificationChannel;
import com.notifications.dto.request.NotificationChannelRequest;
import com.notifications.dto.response.NotificationChannelResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-21T12:06:09+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class NotificationChannelMapperImpl implements NotificationChannelMapper {

    @Override
    public NotificationChannelResponse toResponse(NotificationChannel entity) {
        if ( entity == null ) {
            return null;
        }

        NotificationChannelResponse notificationChannelResponse = new NotificationChannelResponse();

        notificationChannelResponse.setId( entity.getId() );
        notificationChannelResponse.setChannelType( entity.getChannelType() );
        notificationChannelResponse.setStatus( entity.getStatus() );
        notificationChannelResponse.setSlackChannelId( entity.getSlackChannelId() );
        notificationChannelResponse.setWhatsappPhoneNumber( entity.getWhatsappPhoneNumber() );
        notificationChannelResponse.setConsentGiven( entity.isConsentGiven() );
        notificationChannelResponse.setCreatedAt( entity.getCreatedAt() );

        return notificationChannelResponse;
    }

    @Override
    public NotificationChannel toEntity(NotificationChannelRequest request) {
        if ( request == null ) {
            return null;
        }

        NotificationChannel notificationChannel = new NotificationChannel();

        notificationChannel.setChannelType( request.getChannelType() );
        notificationChannel.setBotToken( request.getBotToken() );
        notificationChannel.setSlackChannelId( request.getSlackChannelId() );
        notificationChannel.setWhatsappPhoneNumber( request.getWhatsappPhoneNumber() );
        notificationChannel.setTwilioSid( request.getTwilioSid() );
        notificationChannel.setConsentGiven( request.isConsentGiven() );

        return notificationChannel;
    }

    @Override
    public void updateEntity(NotificationChannelRequest request, NotificationChannel entity) {
        if ( request == null ) {
            return;
        }

        entity.setChannelType( request.getChannelType() );
        entity.setBotToken( request.getBotToken() );
        entity.setSlackChannelId( request.getSlackChannelId() );
        entity.setWhatsappPhoneNumber( request.getWhatsappPhoneNumber() );
        entity.setTwilioSid( request.getTwilioSid() );
        entity.setConsentGiven( request.isConsentGiven() );
    }
}
