package com.ticketinglab.venue.infrastructure.jpa;

import com.ticketinglab.venue.domain.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueJpaRepository extends JpaRepository<Venue, Long> {
    Optional<Venue> findByCode(String code);
    List<Venue> findAllByOrderByCreatedAtDescIdDesc();
    List<Venue> findAllByCreatedByUserIdOrderByCreatedAtDescIdDesc(Long createdByUserId);
}
