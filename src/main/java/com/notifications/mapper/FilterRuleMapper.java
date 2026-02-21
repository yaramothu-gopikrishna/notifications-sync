package com.notifications.mapper;

import com.notifications.domain.FilterRule;
import com.notifications.dto.request.FilterRuleRequest;
import com.notifications.dto.response.FilterRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FilterRuleMapper {

    @Mapping(source = "active", target = "active")
    FilterRuleResponse toResponse(FilterRule entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FilterRule toEntity(FilterRuleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(FilterRuleRequest request, @MappingTarget FilterRule entity);
}
