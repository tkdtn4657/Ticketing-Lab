package com.ticketinglab.hold.domain;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HoldRepository {
    Hold save(Hold hold);
    Optional<Hold> findById(String holdId);
    Optional<Hold> findByIdForUpdate(String holdId);
    List<Hold> findAllActiveExpiredByShowIdAndSeatIdIn(Long showId, Collection<Long> seatIds, LocalDateTime now);
    List<Hold> findAllActiveExpiredByShowIdAndSectionIdIn(Long showId, Collection<Long> sectionIds, LocalDateTime now);
}