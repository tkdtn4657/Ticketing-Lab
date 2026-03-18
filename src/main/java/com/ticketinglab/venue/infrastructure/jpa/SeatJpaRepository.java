package com.ticketinglab.venue.infrastructure.jpa;

import com.ticketinglab.venue.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatJpaRepository extends JpaRepository<Seat, Long> {
}
