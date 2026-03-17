package com.ticketinglab.auth.domain;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
}