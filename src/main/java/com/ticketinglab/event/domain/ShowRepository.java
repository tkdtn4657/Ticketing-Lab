package com.ticketinglab.event.domain;

import java.util.List;
import java.util.Optional;

public interface ShowRepository {
    Show save(Show show);
    Optional<Show> findById(Long showId);
    List<Show> findAllByEventId(Long eventId);
}
