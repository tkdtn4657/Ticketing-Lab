package com.ticketinglab.reservation.infrastructure;

import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.reservation.domain.ReservationStatus;
import com.ticketinglab.reservation.infrastructure.jpa.ReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;

    @Override
    public Reservation save(Reservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        return jpaRepository.findDetailedById(reservationId);
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(String reservationId) {
        return jpaRepository.findLockedById(reservationId);
    }

    @Override
    public Page<Reservation> findPageByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public Page<Reservation> findPageByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable) {
        return jpaRepository.findAllByUserIdAndStatus(userId, status, pageable);
    }

    @Override
    public List<Reservation> findAllPendingExpiredByUserId(Long userId, LocalDateTime now) {
        return jpaRepository.findAllPendingExpiredByUserId(userId, now);
    }

    @Override
    public List<Reservation> findAllPendingExpiredByShowIdAndSeatIdIn(
            Long showId,
            Collection<Long> seatIds,
            LocalDateTime now
    ) {
        return jpaRepository.findAllPendingExpiredByShowIdAndSeatIdIn(showId, now, seatIds);
    }

    @Override
    public List<Reservation> findAllPendingExpiredByShowIdAndSectionIdIn(
            Long showId,
            Collection<Long> sectionIds,
            LocalDateTime now
    ) {
        return jpaRepository.findAllPendingExpiredByShowIdAndSectionIdIn(showId, now, sectionIds);
    }
}