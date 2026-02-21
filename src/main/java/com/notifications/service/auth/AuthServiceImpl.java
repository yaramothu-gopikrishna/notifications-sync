package com.notifications.service.auth;

import com.notifications.config.JwtTokenProvider;
import com.notifications.domain.User;
import com.notifications.dto.request.LoginRequest;
import com.notifications.dto.request.RegisterRequest;
import com.notifications.dto.response.AuthTokenResponse;
import com.notifications.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        log.info("User registered: {}", user.getEmail());
        return buildTokenResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        log.info("User logged in: {}", user.getEmail());
        return buildTokenResponse(user);
    }

    @Override
    public AuthTokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) ||
            !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        var userId = jwtTokenProvider.extractUserId(refreshToken);
        return AuthTokenResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(userId))
                .refreshToken(jwtTokenProvider.generateRefreshToken(userId))
                .expiresIn(900)
                .build();
    }

    private AuthTokenResponse buildTokenResponse(User user) {
        return AuthTokenResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user.getId()))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user.getId()))
                .expiresIn(900)
                .build();
    }
}
