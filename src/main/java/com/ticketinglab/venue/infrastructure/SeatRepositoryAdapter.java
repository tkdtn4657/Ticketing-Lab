package com.ticketinglab.venue.infrastructure;

import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
import com.ticketinglab.venue.infrastructure.jpa.SeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryAdapter implements SeatRepository {

    private final SeatJpaRepository jpaRepository;

    @Override
    public Seat save(Seat seat) {
        return jpaRepository.save(seat);
    }

    @Override
    public List<Seat> saveAll(List<Seat> seats) {
        return jpaRepository.saveAll(seats);
    }

    @Override
    public List<Seat> findAllByVenueId(Long venueId) {
        return jpaRepository.findAllByVenueIdOrderByRowNoAscColNoAscIdAsc(venueId);
    }

    @Override
    public List<Seat> findAllByVenueIdAndIdIn(Long venueId, Collection<Long> seatIds) {
        return jpaRepository.findAllByVenueIdAndIdIn(venueId, seatIds);
    }

    @Override
    public List<Seat> findAllByVenueIdAndLabelIn(Long venueId, Collection<String> labels) {
        return jpaRepository.findAllByVenueIdAndLabelIn(venueId, labels);
    }
}