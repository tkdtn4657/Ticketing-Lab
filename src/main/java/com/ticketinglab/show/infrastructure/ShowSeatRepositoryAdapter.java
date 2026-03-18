package com.ticketinglab.show.infrastructure;

import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.show.infrastructure.jpa.ShowSeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ShowSeatRepositoryAdapter implements ShowSeatRepository {

    private final ShowSeatJpaRepository jpaRepository;

    @Override
    public ShowSeat save(ShowSeat showSeat) {
        return jpaRepository.save(showSeat);
    }

    @Override
    public boolean existsAny() {
        return jpaRepository.count() > 0;
    }

    @Override
    public List<ShowSeat> findAllByShowId(Long showId) {
        return jpaRepository.findAllByShow_Id(showId);
    }
}