package com.notifications.mapper;

import com.notifications.domain.EmailAccount;
import com.notifications.dto.response.EmailAccountResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailAccountMapper {
    EmailAccountResponse toResponse(EmailAccount entity);
}
