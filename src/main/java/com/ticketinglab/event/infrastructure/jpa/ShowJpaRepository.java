package com.ticketinglab.event.infrastructure.jpa;

import com.ticketinglab.event.domain.Show;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowJpaRepository extends JpaRepository<Show, Long> {
    @EntityGraph(attributePaths = "event")
    List<Show> findAllByOrderByStartAtDescIdDesc();

    @EntityGraph(attributePaths = "event")
    List<Show> findAllByCreatedByUserIdOrderByStartAtDescIdDesc(Long createdByUserId);

    List<Show> findAllByEvent_IdOrderByStartAtAscIdAsc(Long eventId);
}
