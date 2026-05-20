package com.ticketinglab.hold.application;

import java.util.Set;

public record SeatHoldQueueTicket(
        Long showId,
        Set<Long> seatIds,
        String owner
) {
}
