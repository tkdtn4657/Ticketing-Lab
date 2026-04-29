package com.ticketinglab.auth.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public record TokenSession(
        Long userId,
        String accessTokenId,
        String accessToken,
        String refreshTokenId,
        String refreshToken
) {

    public TokenSession {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }
        accessTokenId = requireText(accessTokenId, "accessTokenId");
        accessToken = requireText(accessToken, "accessToken");
        refreshTokenId = requireText(refreshTokenId, "refreshTokenId");
        refreshToken = requireText(refreshToken, "refreshToken");
    }

    public static TokenSession issue(
            Long userId,
            String accessTokenId,
            String accessToken,
            String refreshTokenId,
            String refreshToken
    ) {
        return new TokenSession(userId, accessTokenId, accessToken, refreshTokenId, refreshToken);
    }

    public boolean hasAccessToken(String candidate) {
        return tokenEquals(accessToken, candidate);
    }

    public boolean hasRefreshToken(String candidate) {
        return tokenEquals(refreshToken, candidate);
    }

    private static boolean tokenEquals(String expected, String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                candidate.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }
}
