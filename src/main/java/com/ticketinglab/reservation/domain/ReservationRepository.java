package com.ticketinglab.reservation.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(String reservationId);
    Optional<Reservation> findByIdForUpdate(String reservationId);
    Page<Reservation> findPageByUserId(Long userId, Pageable pageable);
    Page<Reservation> findPageByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);
    List<Reservation> findAllPendingExpiredByUserId(Long userId, LocalDateTime now);
    List<Reservation> findAllPendingExpiredByShowIdAndSeatIdIn(Long showId, Collection<Long> seatIds, LocalDateTime now);
    List<Reservation> findAllPendingExpiredByShowIdAndSectionIdIn(Long showId, Collection<Long> sectionIds, LocalDateTime now);
}