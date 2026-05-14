package com.ticketinglab.reservation.application;

import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationItem;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationResourceManager {

    private final ReservationRepository reservationRepository;
    private final ShowSeatRepository showSeatRepository;

    public int expirePendingReservations(LocalDateTime now, int limit) {
        return expirePendingReservationIds(reservationRepository.findPendingExpiredIds(now, limit), now);
    }

    public int expirePendingReservationsByShowId(Long showId, LocalDateTime now, int limit) {
        return expirePendingReservationIds(reservationRepository.findPendingExpiredIdsByShowId(showId, now, limit), now);
    }

    public void expirePendingReservations(
            Long showId,
            Collection<Long> seatIds,
            LocalDateTime now
    ) {
        Map<String, Reservation> expiredReservations = new LinkedHashMap<>();

        if (!seatIds.isEmpty()) {
            reservationRepository.findAllPendingExpiredByShowIdAndSeatIdIn(showId, seatIds, now)
                    .forEach(reservation -> expiredReservations.put(reservation.getId(), reservation));
        }

        expiredReservations.values().forEach(reservation -> expire(reservation, now));
    }

    public void expirePendingReservationsOfUser(Long userId, LocalDateTime now) {
        reservationRepository.findAllPendingExpiredByUserId(userId, now)
                .forEach(reservation -> expire(reservation, now));
    }

    public void expire(Reservation reservation, LocalDateTime now) {
        if (!reservation.isPendingPayment() || !reservation.isExpiredAt(now)) {
            return;
        }

        LockedResources lockedResources = lockResources(
                reservation.getShowId(),
                seatIdsOf(reservation)
        );

        reservation.expire(now);
        releaseResources(reservation, lockedResources);
        reservationRepository.save(reservation);
    }

    public void cancel(Reservation reservation) {
        if (!reservation.isPendingPayment()) {
            reservation.cancel();
            reservationRepository.save(reservation);
            return;
        }

        LockedResources lockedResources = lockResources(
                reservation.getShowId(),
                seatIdsOf(reservation)
        );

        reservation.cancel();
        releaseResources(reservation, lockedResources);
        reservationRepository.save(reservation);
    }

    private int expirePendingReservationIds(Collection<String> reservationIds, LocalDateTime now) {
        int expiredCount = 0;
        for (String reservationId : reservationIds) {
            Reservation reservation = reservationRepository.findByIdForUpdate(reservationId).orElse(null);
            if (reservation == null || !reservation.isPendingPayment() || !reservation.isExpiredAt(now)) {
                continue;
            }
            expire(reservation, now);
            expiredCount++;
        }
        return expiredCount;
    }

    public LockedResources lockResources(
            Long showId,
            Collection<Long> seatIds
    ) {
        Map<Long, ShowSeat> seatById = seatIds.isEmpty()
                ? Map.of()
                : showSeatRepository.findAllByShowIdAndSeatIdInForUpdate(showId, seatIds).stream()
                .collect(Collectors.toMap(showSeat -> showSeat.getSeat().getId(), Function.identity()));

        return new LockedResources(seatById);
    }

    private void releaseResources(Reservation reservation, LockedResources lockedResources) {
        for (ReservationItem item : reservation.getItems()) {
            requiredSeat(lockedResources, item.getSeatId()).releaseReservation();
        }
    }

    private ShowSeat requiredSeat(LockedResources lockedResources, Long seatId) {
        ShowSeat showSeat = lockedResources.seatById().get(seatId);
        if (showSeat == null) {
            throw new IllegalStateException("show seat not found");
        }
        return showSeat;
    }

    private Set<Long> seatIdsOf(Reservation reservation) {
        return reservation.getItems().stream()
                .map(ReservationItem::getSeatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public record LockedResources(
            Map<Long, ShowSeat> seatById
    ) {
    }
}
