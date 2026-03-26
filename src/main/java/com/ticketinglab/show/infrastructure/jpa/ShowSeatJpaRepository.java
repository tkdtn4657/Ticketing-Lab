package com.ticketinglab.show.infrastructure.jpa;

import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShowSeatJpaRepository extends JpaRepository<ShowSeat, ShowSeatId> {

    @EntityGraph(attributePaths = "seat")
    List<ShowSeat> findAllByShow_Id(Long showId);

    List<ShowSeat> findAllByShow_IdAndSeat_IdIn(Long showId, Collection<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select showSeat
            from ShowSeat showSeat
            where showSeat.show.id = :showId
              and showSeat.seat.id in :seatIds
            """)
    List<ShowSeat> findAllByShowIdAndSeatIdInForUpdate(
            @Param("showId") Long showId,
            @Param("seatIds") Collection<Long> seatIds
    );
}
