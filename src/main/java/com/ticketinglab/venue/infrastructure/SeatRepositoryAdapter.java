package com.ticketinglab.venue.infrastructure;

import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
import com.ticketinglab.venue.infrastructure.jpa.SeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryAdapter implements SeatRepository {

    private final SeatJpaRepository jpaRepository;

    @Override
    public Seat save(Seat seat) {
        return jpaRepository.save(seat);
    }
}
