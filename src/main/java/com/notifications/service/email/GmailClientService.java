package com.notifications.service.email;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.notifications.exception.EmailConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class GmailClientService {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String scopes;
    private final String authUri;
    private final String tokenUri;
    private final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public GmailClientService(
            @Value("${gmail.oauth2.client-id}") String clientId,
            @Value("${gmail.oauth2.client-secret}") String clientSecret,
            @Value("${gmail.oauth2.redirect-uri}") String redirectUri,
            @Value("${gmail.oauth2.scopes}") String scopes,
            @Value("${gmail.oauth2.auth-uri}") String authUri,
            @Value("${gmail.oauth2.token-uri}") String tokenUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
        this.authUri = authUri;
        this.tokenUri = tokenUri;
    }

    public String buildAuthorizationUrl(UUID userId) {
        try {
            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    transport, jsonFactory,
                    new GenericUrl(tokenUri),
                    new ClientParametersAuthentication(clientId, clientSecret),
                    clientId, authUri)
                    .setScopes(Collections.singletonList(scopes))
                    .build();

            return flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .setState(userId.toString())
                    .set("access_type", "offline")
                    .set("prompt", "consent")
                    .build();
        } catch (Exception e) {
            throw new EmailConnectionException("Failed to build authorization URL", e);
        }
    }

    public Map<String, String> exchangeCode(String code) {
        try {
            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    transport, jsonFactory,
                    new GenericUrl(tokenUri),
                    new ClientParametersAuthentication(clientId, clientSecret),
                    clientId, authUri)
                    .setScopes(Collections.singletonList(scopes))
                    .build();

            var tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();

            return Map.of(
                    "accessToken", tokenResponse.getAccessToken(),
                    "refreshToken", tokenResponse.getRefreshToken() != null
                            ? tokenResponse.getRefreshToken() : "",
                    "expiresInSeconds", String.valueOf(tokenResponse.getExpiresInSeconds())
            );
        } catch (Exception e) {
            throw new EmailConnectionException("Failed to exchange authorization code", e);
        }
    }

    public Gmail getGmailService(String accessToken) {
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(accessToken);
            return new Gmail.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Email Scan Notify")
                    .build();
        } catch (Exception e) {
            throw new EmailConnectionException("Failed to create Gmail client", e);
        }
    }
}
