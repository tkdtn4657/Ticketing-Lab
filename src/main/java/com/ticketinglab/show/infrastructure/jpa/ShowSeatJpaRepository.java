package com.ticketinglab.show.infrastructure.jpa;

import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowSeatJpaRepository extends JpaRepository<ShowSeat, ShowSeatId> {

    @EntityGraph(attributePaths = "seat")
    List<ShowSeat> findAllByShow_Id(Long showId);
}
