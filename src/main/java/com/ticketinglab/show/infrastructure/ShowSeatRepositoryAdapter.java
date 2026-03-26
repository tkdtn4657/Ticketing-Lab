package com.ticketinglab.show.infrastructure;

import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.show.infrastructure.jpa.ShowSeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
    public List<ShowSeat> saveAll(List<ShowSeat> showSeats) {
        return jpaRepository.saveAll(showSeats);
    }

    @Override
    public boolean existsAny() {
        return jpaRepository.count() > 0;
    }

    @Override
    public List<ShowSeat> findAllByShowId(Long showId) {
        return jpaRepository.findAllByShow_Id(showId);
    }

    @Override
    public List<ShowSeat> findAllByShowIdAndSeatIdIn(Long showId, Collection<Long> seatIds) {
        return jpaRepository.findAllByShow_IdAndSeat_IdIn(showId, seatIds);
    }

    @Override
    public List<ShowSeat> findAllByShowIdAndSeatIdInForUpdate(Long showId, Collection<Long> seatIds) {
        return jpaRepository.findAllByShowIdAndSeatIdInForUpdate(showId, seatIds);
    }
}
