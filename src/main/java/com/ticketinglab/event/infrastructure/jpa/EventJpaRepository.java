package com.ticketinglab.event.infrastructure.jpa;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventJpaRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByOrderByCreatedAtDescIdDesc();
    List<Event> findAllByStatusOrderByCreatedAtDescIdDesc(EventStatus status);
    List<Event> findAllByCreatedByUserIdOrderByCreatedAtDescIdDesc(Long createdByUserId);
}
