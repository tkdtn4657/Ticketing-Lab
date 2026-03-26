package com.ticketinglab.hold.infrastructure.jpa;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
                      and holdItem.sectionId in :sectionIds
              )
            """)
    List<Hold> findAllActiveExpiredByShowIdAndSectionIdIn(
            @Param("showId") Long showId,
            @Param("now") LocalDateTime now,
            @Param("sectionIds") Collection<Long> sectionIds,
            @Param("status") HoldStatus status
    );

    default List<Hold> findAllActiveExpiredByShowIdAndSeatIdIn(
            Long showId,
            LocalDateTime now,
            Collection<Long> seatIds
    ) {
        return findAllActiveExpiredByShowIdAndSeatIdIn(showId, now, seatIds, HoldStatus.ACTIVE);
    }

    default List<Hold> findAllActiveExpiredByShowIdAndSectionIdIn(
            Long showId,
            LocalDateTime now,
            Collection<Long> sectionIds
    ) {
        return findAllActiveExpiredByShowIdAndSectionIdIn(showId, now, sectionIds, HoldStatus.ACTIVE);
    }
}
