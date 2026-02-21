package com.notifications.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @Builder @AllArgsConstructor
public class AuthTokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
