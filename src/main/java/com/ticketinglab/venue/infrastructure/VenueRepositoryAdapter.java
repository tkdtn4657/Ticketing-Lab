package com.ticketinglab.venue.infrastructure;

import com.ticketinglab.venue.domain.Venue;
import com.ticketinglab.venue.domain.VenueRepository;
import com.ticketinglab.venue.infrastructure.jpa.VenueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VenueRepositoryAdapter implements VenueRepository {

    private final VenueJpaRepository jpaRepository;

    @Override
    public Venue save(Venue venue) {
        return jpaRepository.save(venue);
    }

    @Override
    public Optional<Venue> findById(Long venueId) {
        return jpaRepository.findById(venueId);
    }

    @Override
    public Optional<Venue> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }
}