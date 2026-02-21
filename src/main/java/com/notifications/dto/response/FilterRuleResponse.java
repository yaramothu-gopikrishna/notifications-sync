package com.notifications.dto.response;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class FilterRuleResponse {
    private UUID id;
    private String ruleType;
    private String pattern;
    private boolean active;
    private int priority;
    private Instant createdAt;
}
