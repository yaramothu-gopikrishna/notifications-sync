package com.notifications.controller;

import com.notifications.dto.request.FilterRuleRequest;
import com.notifications.dto.response.FilterRuleResponse;
import com.notifications.service.filter.FilterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/filter-rules")
@RequiredArgsConstructor
public class FilterRuleController {

    private final FilterService filterService;

    @PostMapping
    public ResponseEntity<FilterRuleResponse> createRule(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody FilterRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(filterService.createRule(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<FilterRuleResponse>> listRules(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(filterService.listRules(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FilterRuleResponse> updateRule(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody FilterRuleRequest request) {
        return ResponseEntity.ok(filterService.updateRule(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        filterService.deleteRule(userId, id);
        return ResponseEntity.noContent().build();
    }
}
