package com.ticketinglab.hold.infrastructure.jpa;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HoldJpaRepository extends JpaRepository<Hold, String> {

    @Query("""
            select distinct hold
            from Hold hold
            left join fetch hold.items
            where hold.id = :holdId
            """)
    Optional<Hold> findDetailedById(@Param("holdId") String holdId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select hold
            from Hold hold
            where hold.id = :holdId
            """)
    Optional<Hold> findLockedById(@Param("holdId") String holdId);

    @Query("""
            select hold.id
            from Hold hold
            where hold.status = :status
              and hold.expiresAt <= :now
            order by hold.expiresAt asc
            """)
    List<String> findExpiredIds(
            @Param("now") LocalDateTime now,
            @Param("status") HoldStatus status,
            Pageable pageable
    );

    @Query("""
            select hold.id
            from Hold hold
            where hold.showId = :showId
              and hold.status = :status
              and hold.expiresAt <= :now
            order by hold.expiresAt asc
            """)
    List<String> findExpiredIdsByShowId(
            @Param("showId") Long showId,
            @Param("now") LocalDateTime now,
            @Param("status") HoldStatus status,
            Pageable pageable
    );

    @Query("""
            select distinct hold
            from Hold hold
            left join fetch hold.items
            where hold.showId = :showId
              and hold.status = :status
              and hold.expiresAt <= :now
              and exists (
                    select 1
                    from HoldItem holdItem
                    where holdItem.hold = hold
                      and holdItem.seatId in :seatIds
              )
            """)
    List<Hold> findAllActiveExpiredByShowIdAndSeatIdIn(
            @Param("showId") Long showId,
            @Param("now") LocalDateTime now,
            @Param("seatIds") Collection<Long> seatIds,
            @Param("status") HoldStatus status
    );

    default List<Hold> findAllActiveExpiredByShowIdAndSeatIdIn(
            Long showId,
            LocalDateTime now,
            Collection<Long> seatIds
    ) {
        return findAllActiveExpiredByShowIdAndSeatIdIn(showId, now, seatIds, HoldStatus.ACTIVE);
    }

    default List<String> findActiveExpiredIds(LocalDateTime now, int limit) {
        return findExpiredIds(now, HoldStatus.ACTIVE, Pageable.ofSize(limit));
    }

    default List<String> findActiveExpiredIdsByShowId(Long showId, LocalDateTime now, int limit) {
        return findExpiredIdsByShowId(showId, now, HoldStatus.ACTIVE, Pageable.ofSize(limit));
    }
}
