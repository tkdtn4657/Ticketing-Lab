package com.ticketinglab.hold.infrastructure.memory;

import com.ticketinglab.hold.application.SeatHoldPreLock;
import com.ticketinglab.hold.application.SeatHoldPreLockManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.hold.pre-lock.enabled", havingValue = "false")
public class NoOpSeatHoldPreLockManager implements SeatHoldPreLockManager {

    @Override
    public Optional<SeatHoldPreLock> acquire(Long showId, Collection<Long> seatIds, Duration ttl) {
        return Optional.of(SeatHoldPreLock.acquire(showId, seatIds, "noop:" + UUID.randomUUID()));
    }

    @Override
    public void confirmHold(SeatHoldPreLock preLock, String holdId, Duration ttl) {
    }

    @Override
    public void release(SeatHoldPreLock preLock) {
    }

    @Override
    public void releaseHold(Long showId, Collection<Long> seatIds, String holdId) {
    }
}
