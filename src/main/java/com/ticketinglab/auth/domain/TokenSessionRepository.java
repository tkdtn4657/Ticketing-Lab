package com.ticketinglab.auth.domain;

import java.time.Duration;
import java.util.Optional;

public interface TokenSessionRepository {
    void save(TokenSession tokenSession, Duration ttl);
    Optional<TokenSession> findByRefreshTokenId(Long userId, String refreshTokenId);
    boolean hasAccessToken(Long userId, String accessTokenId, String accessToken);
    boolean rotateRefreshToken(
            Long userId,
            String currentRefreshTokenId,
            String currentRefreshToken,
            TokenSession newTokenSession,
            Duration ttl
    );
    boolean deleteByRefreshToken(Long userId, String refreshTokenId, String refreshToken);
    void deleteByUserId(Long userId);
}
