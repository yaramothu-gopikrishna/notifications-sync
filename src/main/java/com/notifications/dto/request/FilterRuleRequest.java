package com.notifications.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FilterRuleRequest {
    @NotBlank
    private String ruleType; // sender or subject_keyword

    @NotBlank
    private String pattern;

    private boolean active = true;
    private int priority = 0;
}
