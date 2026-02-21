package com.notifications.service.filter;

import com.notifications.domain.FilterRule;
import com.notifications.domain.User;
import com.notifications.dto.request.FilterRuleRequest;
import com.notifications.dto.response.FilterRuleResponse;
import com.notifications.exception.ResourceNotFoundException;
import com.notifications.mapper.FilterRuleMapper;
import com.notifications.repository.FilterRuleRepository;
import com.notifications.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilterServiceImpl implements FilterService {

    private final FilterRuleRepository filterRuleRepository;
    private final UserRepository userRepository;
    private final FilterRuleMapper filterRuleMapper;

    @Override
    @Transactional
    public FilterRuleResponse createRule(UUID userId, FilterRuleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        FilterRule rule = filterRuleMapper.toEntity(request);
        rule.setUser(user);
        rule = filterRuleRepository.save(rule);

        log.info("Filter rule created: {} '{}' for user {}", request.getRuleType(), request.getPattern(), userId);
        return filterRuleMapper.toResponse(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilterRuleResponse> listRules(UUID userId) {
        return filterRuleRepository.findByUserId(userId).stream()
                .map(filterRuleMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public FilterRuleResponse updateRule(UUID userId, UUID ruleId, FilterRuleRequest request) {
        FilterRule rule = filterRuleRepository.findByIdAndUserId(ruleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("FilterRule", ruleId));

        filterRuleMapper.updateEntity(request, rule);
        rule = filterRuleRepository.save(rule);

        log.info("Filter rule updated: {} for user {}", ruleId, userId);
        return filterRuleMapper.toResponse(rule);
    }

    @Override
    @Transactional
    public void deleteRule(UUID userId, UUID ruleId) {
        FilterRule rule = filterRuleRepository.findByIdAndUserId(ruleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("FilterRule", ruleId));
        filterRuleRepository.delete(rule);
        log.info("Filter rule deleted: {} for user {}", ruleId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean shouldNotify(UUID userId, String senderAddress, String subject) {
        List<FilterRule> activeRules = filterRuleRepository
                .findByUserIdAndIsActiveOrderByPriorityAsc(userId, true);

        // No rules = notify for all emails (per spec)
        if (activeRules.isEmpty()) {
            return true;
        }

        // If rules exist, at least one must match
        for (FilterRule rule : activeRules) {
            if ("sender".equals(rule.getRuleType())) {
                if (senderAddress != null && senderAddress.equalsIgnoreCase(rule.getPattern())) {
                    return true;
                }
            } else if ("subject_keyword".equals(rule.getRuleType())) {
                if (subject != null && subject.toLowerCase().contains(rule.getPattern().toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }
}
