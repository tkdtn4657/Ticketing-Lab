package com.ticketinglab.venue.domain;

import java.util.Collection;
import java.util.List;

public interface SeatRepository {
    Seat save(Seat seat);
    List<Seat> saveAll(List<Seat> seats);
    List<Seat> findAllByVenueId(Long venueId);
    List<Seat> findAllByVenueIdAndIdIn(Long venueId, Collection<Long> seatIds);
    List<Seat> findAllByVenueIdAndLabelIn(Long venueId, Collection<String> labels);
}