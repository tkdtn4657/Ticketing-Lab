package com.ticketinglab.venue.infrastructure.jpa;

import com.ticketinglab.venue.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SeatJpaRepository extends JpaRepository<Seat, Long> {
    List<Seat> findAllByVenueIdOrderByRowNoAscColNoAscIdAsc(Long venueId);
    List<Seat> findAllByVenueIdAndIdIn(Long venueId, Collection<Long> seatIds);
    List<Seat> findAllByVenueIdAndLabelIn(Long venueId, Collection<String> labels);
}