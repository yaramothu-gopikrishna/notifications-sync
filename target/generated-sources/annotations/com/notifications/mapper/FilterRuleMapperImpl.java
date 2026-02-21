package com.notifications.mapper;

import com.notifications.domain.FilterRule;
import com.notifications.dto.request.FilterRuleRequest;
import com.notifications.dto.response.FilterRuleResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-21T12:06:09+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class FilterRuleMapperImpl implements FilterRuleMapper {

    @Override
    public FilterRuleResponse toResponse(FilterRule entity) {
        if ( entity == null ) {
            return null;
        }

        FilterRuleResponse filterRuleResponse = new FilterRuleResponse();

        filterRuleResponse.setActive( entity.isActive() );
        filterRuleResponse.setId( entity.getId() );
        filterRuleResponse.setRuleType( entity.getRuleType() );
        filterRuleResponse.setPattern( entity.getPattern() );
        filterRuleResponse.setPriority( entity.getPriority() );
        filterRuleResponse.setCreatedAt( entity.getCreatedAt() );

        return filterRuleResponse;
    }

    @Override
    public FilterRule toEntity(FilterRuleRequest request) {
        if ( request == null ) {
            return null;
        }

        FilterRule filterRule = new FilterRule();

        filterRule.setRuleType( request.getRuleType() );
        filterRule.setPattern( request.getPattern() );
        filterRule.setActive( request.isActive() );
        filterRule.setPriority( request.getPriority() );

        return filterRule;
    }

    @Override
    public void updateEntity(FilterRuleRequest request, FilterRule entity) {
        if ( request == null ) {
            return;
        }

        entity.setRuleType( request.getRuleType() );
        entity.setPattern( request.getPattern() );
        entity.setActive( request.isActive() );
        entity.setPriority( request.getPriority() );
    }
}
