package com.ticketinglab.hold.infrastructure.redis;

import com.ticketinglab.hold.application.SeatHoldPreLock;
import com.ticketinglab.hold.application.SeatHoldPreLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.hold.pre-lock.enabled", havingValue = "true", matchIfMissing = true)
public class RedisSeatHoldPreLockManager implements SeatHoldPreLockManager {

    private static final String KEY_PREFIX = "hold:seat:";
    private static final String ATTEMPT_OWNER_PREFIX = "attempt:";
    private static final String HOLD_OWNER_PREFIX = "hold:";

    private static final RedisScript<Long> ACQUIRE_SCRIPT = RedisScript.of("""
            for _, key in ipairs(KEYS) do
                if redis.call('EXISTS', key) == 1 then
                    return 0
                end
            end

            for _, key in ipairs(KEYS) do
                redis.call('SET', key, ARGV[1], 'EX', tonumber(ARGV[2]))
            end

            return 1
            """, Long.class);

    private static final RedisScript<Long> CONFIRM_SCRIPT = RedisScript.of("""
            for _, key in ipairs(KEYS) do
                if redis.call('GET', key) ~= ARGV[1] then
                    return 0
                end
            end

            for _, key in ipairs(KEYS) do
                redis.call('SET', key, ARGV[2], 'EX', tonumber(ARGV[3]))
            end

            return 1
            """, Long.class);

    private static final RedisScript<Long> RELEASE_SCRIPT = RedisScript.of("""
            local released = 0
            for _, key in ipairs(KEYS) do
                if redis.call('GET', key) == ARGV[1] then
                    redis.call('DEL', key)
                    released = released + 1
                end
            end

            return released
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    @Override
    public Optional<SeatHoldPreLock> acquire(Long showId, Collection<Long> seatIds, Duration ttl) {
        String owner = ATTEMPT_OWNER_PREFIX + UUID.randomUUID();
        Long result = redisTemplate.execute(
                ACQUIRE_SCRIPT,
                keys(showId, seatIds),
                owner,
                ttlSeconds(ttl)
        );
        if (result == null || result != 1L) {
            return Optional.empty();
        }
        return Optional.of(SeatHoldPreLock.acquire(showId, seatIds, owner));
    }

    @Override
    public void confirmHold(SeatHoldPreLock preLock, String holdId, Duration ttl) {
        redisTemplate.execute(
                CONFIRM_SCRIPT,
                keys(preLock.showId(), preLock.seatIds()),
                preLock.owner(),
                holdOwner(holdId),
                ttlSeconds(ttl)
        );
    }

    @Override
    public void release(SeatHoldPreLock preLock) {
        releaseByOwner(preLock.showId(), preLock.seatIds(), preLock.owner());
    }

    @Override
    public void releaseHold(Long showId, Collection<Long> seatIds, String holdId) {
        releaseByOwner(showId, seatIds, holdOwner(holdId));
    }

    private void releaseByOwner(Long showId, Collection<Long> seatIds, String owner) {
        redisTemplate.execute(
                RELEASE_SCRIPT,
                keys(showId, seatIds),
                owner
        );
    }

    private List<String> keys(Long showId, Collection<Long> seatIds) {
        return seatIds.stream()
                .distinct()
                .sorted()
                .map(seatId -> KEY_PREFIX + "{" + showId + "}:" + seatId)
                .toList();
    }

    private String holdOwner(String holdId) {
        return HOLD_OWNER_PREFIX + holdId;
    }

    private String ttlSeconds(Duration ttl) {
        return String.valueOf(Math.max(ttl.toSeconds(), 1));
    }
}
