package com.ticketinglab.venue.domain;

import java.util.Optional;

public interface VenueRepository {
    Venue save(Venue venue);
    Optional<Venue> findById(Long venueId);
    Optional<Venue> findByCode(String code);
}