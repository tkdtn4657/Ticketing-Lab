package com.ticketinglab.reservation.infrastructure.jpa;

import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<Reservation, String> {

    @Query("""
            select distinct reservation
            from Reservation reservation
            left join fetch reservation.items
            where reservation.id = :reservationId
            """)
    Optional<Reservation> findDetailedById(@Param("reservationId") String reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select reservation
            from Reservation reservation
            where reservation.id = :reservationId
            """)
    Optional<Reservation> findLockedById(@Param("reservationId") String reservationId);

    Page<Reservation> findAllByUserId(Long userId, Pageable pageable);

    Page<Reservation> findAllByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    @Query("""
            select distinct reservation
            from Reservation reservation
            left join fetch reservation.items
            where reservation.userId = :userId
              and reservation.status = :status
              and reservation.expiresAt <= :now
            """)
    List<Reservation> findAllByUserIdAndStatusAndExpiresAtLessThanEqual(
            @Param("userId") Long userId,
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now
    );

    @Query("""
            select distinct reservation
            from Reservation reservation
            left join fetch reservation.items
            where reservation.showId = :showId
              and reservation.status = :status
              and reservation.expiresAt <= :now
              and exists (
                    select 1
                    from ReservationItem reservationItem
                    where reservationItem.reservation = reservation
                      and reservationItem.seatId in :seatIds
              )
            """)
    List<Reservation> findAllByShowIdAndStatusAndExpiresAtLessThanEqualAndSeatIdIn(
            @Param("showId") Long showId,
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now,
            @Param("seatIds") Collection<Long> seatIds
    );

    @Query("""
            select distinct reservation
            from Reservation reservation
            left join fetch reservation.items
            where reservation.showId = :showId
              and reservation.status = :status
              and reservation.expiresAt <= :now
              and exists (
                    select 1
                    from ReservationItem reservationItem
                    where reservationItem.reservation = reservation
                      and reservationItem.sectionId in :sectionIds
              )
            """)
    List<Reservation> findAllByShowIdAndStatusAndExpiresAtLessThanEqualAndSectionIdIn(
            @Param("showId") Long showId,
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now,
            @Param("sectionIds") Collection<Long> sectionIds
    );

    default List<Reservation> findAllPendingExpiredByUserId(Long userId, LocalDateTime now) {
        return findAllByUserIdAndStatusAndExpiresAtLessThanEqual(userId, ReservationStatus.PENDING_PAYMENT, now);
    }

    default List<Reservation> findAllPendingExpiredByShowIdAndSeatIdIn(
            Long showId,
            LocalDateTime now,
            Collection<Long> seatIds
    ) {
        return findAllByShowIdAndStatusAndExpiresAtLessThanEqualAndSeatIdIn(
                showId,
                ReservationStatus.PENDING_PAYMENT,
                now,
                seatIds
        );
    }

    default List<Reservation> findAllPendingExpiredByShowIdAndSectionIdIn(
            Long showId,
            LocalDateTime now,
            Collection<Long> sectionIds
    ) {
        return findAllByShowIdAndStatusAndExpiresAtLessThanEqualAndSectionIdIn(
                showId,
                ReservationStatus.PENDING_PAYMENT,
                now,
                sectionIds
        );
    }
}