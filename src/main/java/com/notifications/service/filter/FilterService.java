package com.notifications.service.filter;

import com.notifications.dto.request.FilterRuleRequest;
import com.notifications.dto.response.FilterRuleResponse;
import java.util.List;
import java.util.UUID;

public interface FilterService {
    FilterRuleResponse createRule(UUID userId, FilterRuleRequest request);
    List<FilterRuleResponse> listRules(UUID userId);
    FilterRuleResponse updateRule(UUID userId, UUID ruleId, FilterRuleRequest request);
    void deleteRule(UUID userId, UUID ruleId);
    boolean shouldNotify(UUID userId, String senderAddress, String subject);
}
