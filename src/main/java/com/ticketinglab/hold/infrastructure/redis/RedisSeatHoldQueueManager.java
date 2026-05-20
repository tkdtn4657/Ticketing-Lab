package com.ticketinglab.hold.infrastructure.redis;

import com.ticketinglab.hold.application.SeatHoldQueueManager;
import com.ticketinglab.hold.application.SeatHoldQueueTicket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.hold.seat-queue.enabled", havingValue = "true", matchIfMissing = true)
public class RedisSeatHoldQueueManager implements SeatHoldQueueManager {

    private static final String KEY_PREFIX = "hold:seat-queue:";

    private static final RedisScript<Long> ENTER_SCRIPT = RedisScript.of("""
            for _, key in ipairs(KEYS) do
                if redis.call('SCARD', key) >= tonumber(ARGV[2]) then
                    return 0
                end
            end

            for _, key in ipairs(KEYS) do
                redis.call('SADD', key, ARGV[1])
                redis.call('EXPIRE', key, tonumber(ARGV[3]))
            end

            return 1
            """, Long.class);

    private static final RedisScript<Long> LEAVE_SCRIPT = RedisScript.of("""
            local released = 0
            for _, key in ipairs(KEYS) do
                if redis.call('SREM', key, ARGV[1]) == 1 then
                    released = released + 1
                end
            end

            return released
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final int maxPerSeat;

    public RedisSeatHoldQueueManager(
            StringRedisTemplate redisTemplate,
            @Value("${app.hold.seat-queue.max-per-seat:100}") int maxPerSeat
    ) {
        this.redisTemplate = redisTemplate;
        this.maxPerSeat = Math.max(maxPerSeat, 1);
    }

    @Override
    public Optional<SeatHoldQueueTicket> tryEnter(Long showId, Collection<Long> seatIds, Long userId, Duration ttl) {
        Set<Long> normalizedSeatIds = normalizeSeatIds(seatIds);
        String owner = userId + ":" + UUID.randomUUID();
        Long result = redisTemplate.execute(
                ENTER_SCRIPT,
                keys(showId, normalizedSeatIds),
                owner,
                String.valueOf(maxPerSeat),
                ttlSeconds(ttl)
        );
        if (result == null || result != 1L) {
            return Optional.empty();
        }
        return Optional.of(new SeatHoldQueueTicket(showId, normalizedSeatIds, owner));
    }

    @Override
    public void leave(SeatHoldQueueTicket ticket) {
        redisTemplate.execute(
                LEAVE_SCRIPT,
                keys(ticket.showId(), ticket.seatIds()),
                ticket.owner()
        );
    }

    private Set<Long> normalizeSeatIds(Collection<Long> seatIds) {
        return seatIds.stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> keys(Long showId, Collection<Long> seatIds) {
        return seatIds.stream()
                .map(seatId -> KEY_PREFIX + "{" + showId + "}:" + seatId)
                .toList();
    }

    private String ttlSeconds(Duration ttl) {
        return String.valueOf(Math.max(ttl.toSeconds(), 1));
    }
}
