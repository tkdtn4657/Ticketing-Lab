package com.ticketinglab.event.infrastructure;

import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.event.infrastructure.jpa.ShowJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShowRepositoryAdapter implements ShowRepository {

    private final ShowJpaRepository jpaRepository;

    @Override
    public Show save(Show show) {
        return jpaRepository.save(show);
    }

    @Override
    public Optional<Show> findById(Long showId) {
        return jpaRepository.findById(showId);
    }

    @Override
    public List<Show> findAllByEventId(Long eventId) {
        return jpaRepository.findAllByEvent_IdOrderByStartAtAscIdAsc(eventId);
    }
}
