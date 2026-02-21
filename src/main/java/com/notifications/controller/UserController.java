package com.notifications.controller;

import com.notifications.domain.User;
import com.notifications.dto.response.UserProfileResponse;
import com.notifications.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String gravatarUrl = "https://www.gravatar.com/avatar/" + md5(user.getEmail().trim().toLowerCase()) + "?d=identicon&s=80";

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .notificationsPaused(user.isNotificationsPaused())
                .createdAt(user.getCreatedAt())
                .gravatarUrl(gravatarUrl)
                .build());
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "00000000000000000000000000000000";
        }
    }
}
