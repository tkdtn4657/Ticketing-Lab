package com.ticketinglab.hold.infrastructure;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.hold.infrastructure.jpa.HoldJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HoldRepositoryAdapter implements HoldRepository {

    private final HoldJpaRepository jpaRepository;

    @Override
    public Hold save(Hold hold) {
        return jpaRepository.save(hold);
    }

    @Override
    public Optional<Hold> findById(String holdId) {
        return jpaRepository.findDetailedById(holdId);
    }

    @Override
    public Optional<Hold> findByIdForUpdate(String holdId) {
        return jpaRepository.findLockedById(holdId);
    }

    @Override
    public List<Hold> findAllActiveExpiredByShowIdAndSeatIdIn(
            Long showId,
            Collection<Long> seatIds,
            LocalDateTime now
    ) {
        return jpaRepository.findAllActiveExpiredByShowIdAndSeatIdIn(showId, now, seatIds);
    }

    @Override
    public List<Hold> findAllActiveExpiredByShowIdAndSectionIdIn(
            Long showId,
            Collection<Long> sectionIds,
            LocalDateTime now
    ) {
        return jpaRepository.findAllActiveExpiredByShowIdAndSectionIdIn(showId, now, sectionIds);
    }
}