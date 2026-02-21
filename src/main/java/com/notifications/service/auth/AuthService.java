package com.notifications.service.auth;

import com.notifications.dto.request.LoginRequest;
import com.notifications.dto.request.RegisterRequest;
import com.notifications.dto.response.AuthTokenResponse;

public interface AuthService {
    AuthTokenResponse register(RegisterRequest request);
    AuthTokenResponse login(LoginRequest request);
    AuthTokenResponse refreshToken(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
