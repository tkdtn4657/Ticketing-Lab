package com.ticketinglab.show.infrastructure.jpa;

import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShowSeatJpaRepository extends JpaRepository<ShowSeat, ShowSeatId> {

    @EntityGraph(attributePaths = "seat")
    List<ShowSeat> findAllByShow_Id(Long showId);

    List<ShowSeat> findAllByShow_IdAndSeat_IdIn(Long showId, Collection<Long> seatIds);
}