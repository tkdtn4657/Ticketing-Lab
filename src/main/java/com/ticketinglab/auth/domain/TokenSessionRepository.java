package com.ticketinglab.auth.domain;

import java.time.Duration;
import java.util.Optional;

public interface TokenSessionRepository {
    void save(TokenSession tokenSession, Duration ttl);
    Optional<TokenSession> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
