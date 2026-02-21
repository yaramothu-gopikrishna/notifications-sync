package com.notifications.controller;

import com.notifications.dto.request.NotificationChannelRequest;
import com.notifications.dto.response.NotificationChannelResponse;
import com.notifications.service.notification.NotificationChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-channels")
@RequiredArgsConstructor
public class NotificationChannelController {

    private final NotificationChannelService channelService;

    @PostMapping
    public ResponseEntity<NotificationChannelResponse> createChannel(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody NotificationChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(channelService.createChannel(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<NotificationChannelResponse>> listChannels(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(channelService.listChannels(userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NotificationChannelResponse> updateChannel(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody NotificationChannelRequest request) {
        return ResponseEntity.ok(channelService.updateChannel(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        channelService.deleteChannel(userId, id);
        return ResponseEntity.noContent().build();
    }
}
