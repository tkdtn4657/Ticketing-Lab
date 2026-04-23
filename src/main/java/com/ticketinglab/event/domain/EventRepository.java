package com.ticketinglab.event.domain;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Event save(Event event);
    boolean existsAny();
    Optional<Event> findById(Long eventId);
    List<Event> findAll();
    List<Event> findAllByStatus(EventStatus status);
    List<Event> findAllByCreatedByUserId(Long userId);
}
