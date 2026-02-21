package com.notifications.mapper;

import com.notifications.domain.EmailAccount;
import com.notifications.dto.response.EmailAccountResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-21T12:06:09+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class EmailAccountMapperImpl implements EmailAccountMapper {

    @Override
    public EmailAccountResponse toResponse(EmailAccount entity) {
        if ( entity == null ) {
            return null;
        }

        EmailAccountResponse emailAccountResponse = new EmailAccountResponse();

        emailAccountResponse.setId( entity.getId() );
        emailAccountResponse.setProvider( entity.getProvider() );
        emailAccountResponse.setEmailAddress( entity.getEmailAddress() );
        emailAccountResponse.setStatus( entity.getStatus() );
        emailAccountResponse.setLastScannedAt( entity.getLastScannedAt() );
        emailAccountResponse.setCreatedAt( entity.getCreatedAt() );

        return emailAccountResponse;
    }
}
