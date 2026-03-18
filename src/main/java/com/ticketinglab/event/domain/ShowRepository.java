package com.ticketinglab.event.domain;

import java.util.List;

public interface ShowRepository {
    Show save(Show show);
    List<Show> findAllByEventId(Long eventId);
}
