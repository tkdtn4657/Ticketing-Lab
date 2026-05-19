package com.ticketinglab.hold.application;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

public interface SeatHoldPreLockManager {

    Optional<SeatHoldPreLock> acquire(Long showId, Collection<Long> seatIds, Duration ttl);

    void confirmHold(SeatHoldPreLock preLock, String holdId, Duration ttl);

    void release(SeatHoldPreLock preLock);

    void releaseHold(Long showId, Collection<Long> seatIds, String holdId);
}
