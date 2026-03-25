package com.ticketinglab.show.domain;

import java.util.Collection;
import java.util.List;

public interface ShowSeatRepository {
    ShowSeat save(ShowSeat showSeat);
    List<ShowSeat> saveAll(List<ShowSeat> showSeats);
    boolean existsAny();
    List<ShowSeat> findAllByShowId(Long showId);
    List<ShowSeat> findAllByShowIdAndSeatIdIn(Long showId, Collection<Long> seatIds);
}