package com.ticketinglab.show.domain;

import java.util.List;

public interface ShowSeatRepository {
    ShowSeat save(ShowSeat showSeat);
    boolean existsAny();
    List<ShowSeat> findAllByShowId(Long showId);
}