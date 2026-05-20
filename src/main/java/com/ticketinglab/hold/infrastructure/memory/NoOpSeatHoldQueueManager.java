package com.ticketinglab.hold.infrastructure.memory;

import com.ticketinglab.hold.application.SeatHoldQueueManager;
import com.ticketinglab.hold.application.SeatHoldQueueTicket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.hold.seat-queue.enabled", havingValue = "false")
public class NoOpSeatHoldQueueManager implements SeatHoldQueueManager {

    @Override
    public Optional<SeatHoldQueueTicket> tryEnter(Long showId, Collection<Long> seatIds, Long userId, Duration ttl) {
        return Optional.of(new SeatHoldQueueTicket(
                showId,
                seatIds.stream()
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                "noop:" + userId + ":" + UUID.randomUUID()
        ));
    }

    @Override
    public void leave(SeatHoldQueueTicket ticket) {
    }
}
