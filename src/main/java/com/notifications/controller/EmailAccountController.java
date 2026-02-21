package com.notifications.controller;

import com.notifications.dto.response.EmailAccountResponse;
import com.notifications.service.email.EmailAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email-accounts")
@RequiredArgsConstructor
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    @PostMapping("/connect")
    public ResponseEntity<Map<String, String>> initiateConnect(@AuthenticationPrincipal UUID userId) {
        String authUrl = emailAccountService.initiateConnection(userId);
        return ResponseEntity.ok(Map.of("authorizationUrl", authUrl));
    }

    @GetMapping("/callback")
    public ResponseEntity<EmailAccountResponse> callback(
            @RequestParam String code, @RequestParam String state) {
        UUID userId = UUID.fromString(state);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emailAccountService.completeConnection(userId, code));
    }

    @GetMapping
    public ResponseEntity<List<EmailAccountResponse>> listAccounts(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(emailAccountService.listAccounts(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailAccountResponse> getAccount(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return ResponseEntity.ok(emailAccountService.getAccount(userId, id));
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<EmailAccountResponse> pauseAccount(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return ResponseEntity.ok(emailAccountService.pauseAccount(userId, id));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<EmailAccountResponse> resumeAccount(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return ResponseEntity.ok(emailAccountService.resumeAccount(userId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disconnectAccount(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        emailAccountService.disconnectAccount(userId, id);
        return ResponseEntity.noContent().build();
    }
}
