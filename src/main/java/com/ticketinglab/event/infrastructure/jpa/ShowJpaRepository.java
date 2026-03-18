package com.ticketinglab.event.infrastructure.jpa;

import com.ticketinglab.event.domain.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowJpaRepository extends JpaRepository<Show, Long> {
    List<Show> findAllByEvent_IdOrderByStartAtAscIdAsc(Long eventId);
}
