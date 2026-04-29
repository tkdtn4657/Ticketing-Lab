package com.ticketinglab.auth.infrastructure.memory;

import com.ticketinglab.auth.domain.TokenSession;
import com.ticketinglab.auth.domain.TokenSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
@ConditionalOnProperty(name = "app.auth.token-session.store", havingValue = "in-memory")
public class InMemoryTokenSessionRepository implements TokenSessionRepository {

    private final ConcurrentMap<Long, StoredTokenSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(TokenSession tokenSession, Duration ttl) {
        sessions.put(tokenSession.userId(), new StoredTokenSession(tokenSession, Instant.now().plus(ttl)));
    }

    @Override
    public Optional<TokenSession> findByUserId(Long userId) {
        StoredTokenSession storedSession = sessions.get(userId);
        if (storedSession == null) {
            return Optional.empty();
        }

        if (storedSession.isExpired()) {
            sessions.remove(userId, storedSession);
            return Optional.empty();
        }

        return Optional.of(storedSession.tokenSession());
    }

    @Override
    public void deleteByUserId(Long userId) {
        sessions.remove(userId);
    }

    private record StoredTokenSession(
            TokenSession tokenSession,
            Instant expiresAt
    ) {

        private boolean isExpired() {
            return !expiresAt.isAfter(Instant.now());
        }
    }
}
