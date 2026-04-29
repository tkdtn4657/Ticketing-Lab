package com.ticketinglab.auth.infrastructure.memory;

import com.ticketinglab.auth.domain.TokenSession;
import com.ticketinglab.auth.domain.TokenSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "app.auth.token-session.store", havingValue = "in-memory")
public class InMemoryTokenSessionRepository implements TokenSessionRepository {

    @Value("${app.auth.token-session.max-sessions-per-user:5}")
    private int maxSessionsPerUser;

    private final ConcurrentMap<Long, ConcurrentMap<String, StoredTokenSession>> sessions = new ConcurrentHashMap<>();
    private final AtomicLong sessionOrder = new AtomicLong();

    @Override
    public synchronized void save(TokenSession tokenSession, Duration ttl) {
        Instant now = Instant.now();
        ConcurrentMap<String, StoredTokenSession> userSessions = sessionsOf(tokenSession.userId());
        removeExpired(userSessions, now);
        userSessions.put(
                tokenSession.refreshTokenId(),
                new StoredTokenSession(tokenSession, sessionOrder.getAndIncrement(), now.plus(ttl))
        );
        pruneOldest(userSessions);
    }

    @Override
    public synchronized Optional<TokenSession> findByRefreshTokenId(Long userId, String refreshTokenId) {
        ConcurrentMap<String, StoredTokenSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return Optional.empty();
        }

        removeExpired(userSessions, Instant.now());

        StoredTokenSession storedSession = userSessions.get(refreshTokenId);
        if (storedSession == null) {
            return Optional.empty();
        }

        return Optional.of(storedSession.tokenSession());
    }

    @Override
    public synchronized boolean hasAccessToken(Long userId, String accessTokenId, String accessToken) {
        ConcurrentMap<String, StoredTokenSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return false;
        }

        removeExpired(userSessions, Instant.now());

        return userSessions.values().stream()
                .map(StoredTokenSession::tokenSession)
                .anyMatch(tokenSession -> tokenSession.accessTokenId().equals(accessTokenId)
                        && tokenSession.hasAccessToken(accessToken));
    }

    @Override
    public synchronized boolean rotateRefreshToken(
            Long userId,
            String currentRefreshTokenId,
            String currentRefreshToken,
            TokenSession newTokenSession,
            Duration ttl
    ) {
        ConcurrentMap<String, StoredTokenSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return false;
        }

        Instant now = Instant.now();
        removeExpired(userSessions, now);

        StoredTokenSession currentSession = userSessions.get(currentRefreshTokenId);
        if (currentSession == null || !currentSession.tokenSession().hasRefreshToken(currentRefreshToken)) {
            return false;
        }

        userSessions.remove(currentRefreshTokenId);
        userSessions.put(
                newTokenSession.refreshTokenId(),
                new StoredTokenSession(newTokenSession, sessionOrder.getAndIncrement(), now.plus(ttl))
        );
        pruneOldest(userSessions);
        return true;
    }

    @Override
    public synchronized boolean deleteByRefreshToken(Long userId, String refreshTokenId, String refreshToken) {
        ConcurrentMap<String, StoredTokenSession> userSessions = sessions.get(userId);
        if (userSessions == null) {
            return false;
        }

        removeExpired(userSessions, Instant.now());

        StoredTokenSession currentSession = userSessions.get(refreshTokenId);
        if (currentSession == null || !currentSession.tokenSession().hasRefreshToken(refreshToken)) {
            return false;
        }

        userSessions.remove(refreshTokenId);
        return true;
    }

    @Override
    public synchronized void deleteByUserId(Long userId) {
        sessions.remove(userId);
    }

    private ConcurrentMap<String, StoredTokenSession> sessionsOf(Long userId) {
        return sessions.computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>());
    }

    private void removeExpired(ConcurrentMap<String, StoredTokenSession> userSessions, Instant now) {
        userSessions.entrySet()
                .removeIf(entry -> entry.getValue().isExpiredAt(now));
    }

    private void pruneOldest(ConcurrentMap<String, StoredTokenSession> userSessions) {
        int overflow = userSessions.size() - maxSessionsPerUser();
        if (overflow <= 0) {
            return;
        }

        userSessions.entrySet().stream()
                .sorted(Comparator.comparingLong(entry -> entry.getValue().issuedOrder()))
                .limit(overflow)
                .map(Map.Entry::getKey)
                .toList()
                .forEach(userSessions::remove);
    }

    private int maxSessionsPerUser() {
        return Math.max(maxSessionsPerUser, 1);
    }

    private record StoredTokenSession(
            TokenSession tokenSession,
            long issuedOrder,
            Instant expiresAt
    ) {

        private boolean isExpiredAt(Instant now) {
            return !expiresAt.isAfter(now);
        }
    }
}
