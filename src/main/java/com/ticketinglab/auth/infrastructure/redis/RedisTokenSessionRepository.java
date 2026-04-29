package com.ticketinglab.auth.infrastructure.redis;

import com.ticketinglab.auth.domain.TokenSession;
import com.ticketinglab.auth.domain.TokenSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.auth.token-session.store", havingValue = "redis", matchIfMissing = true)
public class RedisTokenSessionRepository implements TokenSessionRepository {

    private static final String KEY_PREFIX = "auth:session:";
    private static final String ACCESS_TOKEN_FIELD = "accessToken";
    private static final String REFRESH_TOKEN_FIELD = "refreshToken";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(TokenSession tokenSession, Duration ttl) {
        String key = key(tokenSession.userId());
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        hashOperations.put(key, ACCESS_TOKEN_FIELD, tokenSession.accessToken());
        hashOperations.put(key, REFRESH_TOKEN_FIELD, tokenSession.refreshToken());
        redisTemplate.expire(key, ttl);
    }

    @Override
    public Optional<TokenSession> findByUserId(Long userId) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String key = key(userId);

        String accessToken = hashOperations.get(key, ACCESS_TOKEN_FIELD);
        String refreshToken = hashOperations.get(key, REFRESH_TOKEN_FIELD);
        if (accessToken == null || refreshToken == null) {
            return Optional.empty();
        }

        return Optional.of(TokenSession.issue(userId, accessToken, refreshToken));
    }

    @Override
    public void deleteByUserId(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
