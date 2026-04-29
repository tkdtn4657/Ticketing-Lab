package com.ticketinglab.auth.infrastructure.redis;

import com.ticketinglab.auth.domain.TokenSession;
import com.ticketinglab.auth.domain.TokenSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.auth.token-session.store", havingValue = "redis", matchIfMissing = true)
public class RedisTokenSessionRepository implements TokenSessionRepository {

    private static final String USER_SESSIONS_KEY_PREFIX = "auth:user:sessions:";
    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final String ACCESS_KEY_PREFIX = "auth:access:";
    private static final String ACCESS_TOKEN_ID_FIELD = "accessTokenId";
    private static final String ACCESS_TOKEN_FIELD = "accessToken";
    private static final String REFRESH_TOKEN_FIELD = "refreshToken";

    private static final RedisScript<Long> SAVE_SCRIPT = RedisScript.of("""
            redis.call('HSET', KEYS[2],
                    'accessTokenId', ARGV[2],
                    'accessToken', ARGV[3],
                    'refreshToken', ARGV[4])
            redis.call('EXPIRE', KEYS[2], tonumber(ARGV[6]))
            redis.call('SET', KEYS[3], ARGV[1], 'EX', tonumber(ARGV[6]))
            redis.call('ZADD', KEYS[1], tonumber(ARGV[5]), ARGV[1])
            redis.call('EXPIRE', KEYS[1], tonumber(ARGV[6]))

            local overflow = redis.call('ZCARD', KEYS[1]) - tonumber(ARGV[7])
            if overflow > 0 then
                local expiredIds = redis.call('ZRANGE', KEYS[1], 0, overflow - 1)
                for _, refreshTokenId in ipairs(expiredIds) do
                    local sessionKey = ARGV[8] .. refreshTokenId
                    local accessTokenId = redis.call('HGET', sessionKey, 'accessTokenId')
                    if accessTokenId then
                        redis.call('DEL', ARGV[9] .. accessTokenId)
                    end
                    redis.call('DEL', sessionKey)
                    redis.call('ZREM', KEYS[1], refreshTokenId)
                end
            end

            return 1
            """, Long.class);

    private static final RedisScript<Long> ROTATE_SCRIPT = RedisScript.of("""
            local storedRefreshToken = redis.call('HGET', KEYS[2], 'refreshToken')
            if not storedRefreshToken or storedRefreshToken ~= ARGV[2] then
                return 0
            end

            local currentAccessTokenId = redis.call('HGET', KEYS[2], 'accessTokenId')
            if currentAccessTokenId then
                redis.call('DEL', ARGV[11] .. currentAccessTokenId)
            end
            redis.call('DEL', KEYS[2])
            redis.call('ZREM', KEYS[1], ARGV[1])

            redis.call('HSET', KEYS[3],
                    'accessTokenId', ARGV[4],
                    'accessToken', ARGV[5],
                    'refreshToken', ARGV[6])
            redis.call('EXPIRE', KEYS[3], tonumber(ARGV[8]))
            redis.call('SET', KEYS[4], ARGV[3], 'EX', tonumber(ARGV[8]))
            redis.call('ZADD', KEYS[1], tonumber(ARGV[7]), ARGV[3])
            redis.call('EXPIRE', KEYS[1], tonumber(ARGV[8]))

            local overflow = redis.call('ZCARD', KEYS[1]) - tonumber(ARGV[9])
            if overflow > 0 then
                local expiredIds = redis.call('ZRANGE', KEYS[1], 0, overflow - 1)
                for _, refreshTokenId in ipairs(expiredIds) do
                    local sessionKey = ARGV[10] .. refreshTokenId
                    local accessTokenId = redis.call('HGET', sessionKey, 'accessTokenId')
                    if accessTokenId then
                        redis.call('DEL', ARGV[11] .. accessTokenId)
                    end
                    redis.call('DEL', sessionKey)
                    redis.call('ZREM', KEYS[1], refreshTokenId)
                end
            end

            return 1
            """, Long.class);

    private static final RedisScript<Long> DELETE_BY_REFRESH_SCRIPT = RedisScript.of("""
            local storedRefreshToken = redis.call('HGET', KEYS[2], 'refreshToken')
            if not storedRefreshToken or storedRefreshToken ~= ARGV[2] then
                return 0
            end

            local accessTokenId = redis.call('HGET', KEYS[2], 'accessTokenId')
            if accessTokenId then
                redis.call('DEL', ARGV[3] .. accessTokenId)
            end
            redis.call('DEL', KEYS[2])
            redis.call('ZREM', KEYS[1], ARGV[1])
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final AtomicLong scoreSequence = new AtomicLong();

    @Value("${app.auth.token-session.max-sessions-per-user:5}")
    private int maxSessionsPerUser;

    @Override
    public void save(TokenSession tokenSession, Duration ttl) {
        redisTemplate.execute(
                SAVE_SCRIPT,
                List.of(
                        userSessionsKey(tokenSession.userId()),
                        sessionKey(tokenSession.userId(), tokenSession.refreshTokenId()),
                        accessKey(tokenSession.userId(), tokenSession.accessTokenId())
                ),
                tokenSession.refreshTokenId(),
                tokenSession.accessTokenId(),
                tokenSession.accessToken(),
                tokenSession.refreshToken(),
                score(),
                ttlSeconds(ttl),
                maxSessionsPerUser(),
                sessionKeyPrefix(tokenSession.userId()),
                accessKeyPrefix(tokenSession.userId())
        );
    }

    @Override
    public Optional<TokenSession> findByRefreshTokenId(Long userId, String refreshTokenId) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String sessionKey = sessionKey(userId, refreshTokenId);

        String accessTokenId = hashOperations.get(sessionKey, ACCESS_TOKEN_ID_FIELD);
        String accessToken = hashOperations.get(sessionKey, ACCESS_TOKEN_FIELD);
        String refreshToken = hashOperations.get(sessionKey, REFRESH_TOKEN_FIELD);
        if (accessTokenId == null || accessToken == null || refreshToken == null) {
            return Optional.empty();
        }

        return Optional.of(TokenSession.issue(userId, accessTokenId, accessToken, refreshTokenId, refreshToken));
    }

    @Override
    public boolean hasAccessToken(Long userId, String accessTokenId, String accessToken) {
        String refreshTokenId = redisTemplate.opsForValue().get(accessKey(userId, accessTokenId));
        if (refreshTokenId == null) {
            return false;
        }

        return findByRefreshTokenId(userId, refreshTokenId)
                .map(tokenSession -> tokenSession.hasAccessToken(accessToken))
                .orElse(false);
    }

    @Override
    public boolean rotateRefreshToken(
            Long userId,
            String currentRefreshTokenId,
            String currentRefreshToken,
            TokenSession newTokenSession,
            Duration ttl
    ) {
        Long result = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(
                        userSessionsKey(userId),
                        sessionKey(userId, currentRefreshTokenId),
                        sessionKey(userId, newTokenSession.refreshTokenId()),
                        accessKey(userId, newTokenSession.accessTokenId())
                ),
                currentRefreshTokenId,
                currentRefreshToken,
                newTokenSession.refreshTokenId(),
                newTokenSession.accessTokenId(),
                newTokenSession.accessToken(),
                newTokenSession.refreshToken(),
                score(),
                ttlSeconds(ttl),
                maxSessionsPerUser(),
                sessionKeyPrefix(userId),
                accessKeyPrefix(userId)
        );
        return result != null && result == 1L;
    }

    @Override
    public boolean deleteByRefreshToken(Long userId, String refreshTokenId, String refreshToken) {
        Long result = redisTemplate.execute(
                DELETE_BY_REFRESH_SCRIPT,
                List.of(
                        userSessionsKey(userId),
                        sessionKey(userId, refreshTokenId)
                ),
                refreshTokenId,
                refreshToken,
                accessKeyPrefix(userId)
        );
        return result != null && result == 1L;
    }

    @Override
    public void deleteByUserId(Long userId) {
        String userSessionsKey = userSessionsKey(userId);
        Set<String> refreshTokenIds = redisTemplate.opsForZSet().range(userSessionsKey, 0, -1);
        if (refreshTokenIds == null) {
            return;
        }

        for (String refreshTokenId : refreshTokenIds) {
            findByRefreshTokenId(userId, refreshTokenId)
                    .ifPresent(tokenSession -> redisTemplate.delete(accessKey(userId, tokenSession.accessTokenId())));
            redisTemplate.delete(sessionKey(userId, refreshTokenId));
        }
        redisTemplate.delete(userSessionsKey);
    }

    private String userSessionsKey(Long userId) {
        return USER_SESSIONS_KEY_PREFIX + userId;
    }

    private String sessionKey(Long userId, String refreshTokenId) {
        return sessionKeyPrefix(userId) + refreshTokenId;
    }

    private String sessionKeyPrefix(Long userId) {
        return SESSION_KEY_PREFIX + userId + ":";
    }

    private String accessKey(Long userId, String accessTokenId) {
        return accessKeyPrefix(userId) + accessTokenId;
    }

    private String accessKeyPrefix(Long userId) {
        return ACCESS_KEY_PREFIX + userId + ":";
    }

    private String score() {
        long sequence = Math.floorMod(scoreSequence.getAndIncrement(), 1000);
        return String.valueOf(Instant.now().toEpochMilli() * 1000 + sequence);
    }

    private String ttlSeconds(Duration ttl) {
        return String.valueOf(Math.max(ttl.getSeconds(), 1));
    }

    private String maxSessionsPerUser() {
        return String.valueOf(Math.max(maxSessionsPerUser, 1));
    }
}
