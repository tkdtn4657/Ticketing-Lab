package com.ticketinglab.venue.domain;

import java.util.List;
import java.util.Optional;

public interface VenueRepository {
    Venue save(Venue venue);
    Optional<Venue> findById(Long venueId);
    Optional<Venue> findByCode(String code);
    List<Venue> findAll();
    List<Venue> findAllByCreatedByUserId(Long userId);
}
