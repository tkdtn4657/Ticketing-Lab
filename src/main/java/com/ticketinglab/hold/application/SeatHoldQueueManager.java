package com.ticketinglab.hold.application;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

public interface SeatHoldQueueManager {

    Optional<SeatHoldQueueTicket> tryEnter(Long showId, Collection<Long> seatIds, Long userId, Duration ttl);

    void leave(SeatHoldQueueTicket ticket);
}
