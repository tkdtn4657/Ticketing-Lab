package com.ticketinglab.hold.application;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public record SeatHoldPreLock(
        Long showId,
        Set<Long> seatIds,
        String owner
) {

    public SeatHoldPreLock {
        seatIds = Set.copyOf(new LinkedHashSet<>(seatIds));
    }

    public static SeatHoldPreLock acquire(Long showId, Collection<Long> seatIds, String owner) {
        return new SeatHoldPreLock(showId, new LinkedHashSet<>(seatIds), owner);
    }
}
