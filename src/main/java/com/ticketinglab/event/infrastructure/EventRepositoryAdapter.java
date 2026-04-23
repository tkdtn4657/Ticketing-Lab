package com.ticketinglab.event.infrastructure;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.infrastructure.jpa.EventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepositoryAdapter implements EventRepository {

    private final EventJpaRepository jpaRepository;

    @Override
    public Event save(Event event) {
        return jpaRepository.save(event);
    }

    @Override
    public boolean existsAny() {
        return jpaRepository.count() > 0;
    }

    @Override
    public Optional<Event> findById(Long eventId) {
        return jpaRepository.findById(eventId);
    }

    @Override
    public List<Event> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    @Override
    public List<Event> findAllByStatus(EventStatus status) {
        return jpaRepository.findAllByStatusOrderByCreatedAtDescIdDesc(status);
    }

    @Override
    public List<Event> findAllByCreatedByUserId(Long userId) {
        return jpaRepository.findAllByCreatedByUserIdOrderByCreatedAtDescIdDesc(userId);
    }
}
