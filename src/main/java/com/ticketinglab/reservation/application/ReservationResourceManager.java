package com.ticketinglab.reservation.application;

import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationItem;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationResourceManager {

    private final ReservationRepository reservationRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowSectionInventoryRepository showSectionInventoryRepository;

    public void expirePendingReservations(
            Long showId,
            Collection<Long> seatIds,
            Collection<Long> sectionIds,
            LocalDateTime now
    ) {
        Map<String, Reservation> expiredReservations = new LinkedHashMap<>();

        if (!seatIds.isEmpty()) {
            reservationRepository.findAllPendingExpiredByShowIdAndSeatIdIn(showId, seatIds, now)
                    .forEach(reservation -> expiredReservations.put(reservation.getId(), reservation));
        }

        if (!sectionIds.isEmpty()) {
            reservationRepository.findAllPendingExpiredByShowIdAndSectionIdIn(showId, sectionIds, now)
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
                seatIdsOf(reservation),
                sectionIdsOf(reservation)
        );

        reservation.expire(now);
        releaseResources(reservation, lockedResources);
        reservationRepository.save(reservation);
    }

    public LockedResources lockResources(
            Long showId,
            Collection<Long> seatIds,
            Collection<Long> sectionIds
    ) {
        Map<Long, ShowSeat> seatById = seatIds.isEmpty()
                ? Map.of()
                : showSeatRepository.findAllByShowIdAndSeatIdInForUpdate(showId, seatIds).stream()
                .collect(Collectors.toMap(showSeat -> showSeat.getSeat().getId(), Function.identity()));

        Map<Long, ShowSectionInventory> sectionById = sectionIds.isEmpty()
                ? Map.of()
                : showSectionInventoryRepository.findAllByShowIdAndSectionIdInForUpdate(showId, sectionIds).stream()
                .collect(Collectors.toMap(inventory -> inventory.getSection().getId(), Function.identity()));

        return new LockedResources(seatById, sectionById);
    }

    private void releaseResources(Reservation reservation, LockedResources lockedResources) {
        for (ReservationItem item : reservation.getItems()) {
            if (item.getSeatId() != null) {
                requiredSeat(lockedResources, item.getSeatId()).releaseReservation();
            }
            if (item.getSectionId() != null) {
                requiredSection(lockedResources, item.getSectionId()).releaseHold(item.getQty());
            }
        }
    }

    private ShowSeat requiredSeat(LockedResources lockedResources, Long seatId) {
        ShowSeat showSeat = lockedResources.seatById().get(seatId);
        if (showSeat == null) {
            throw new IllegalStateException("show seat not found");
        }
        return showSeat;
    }

    private ShowSectionInventory requiredSection(LockedResources lockedResources, Long sectionId) {
        ShowSectionInventory inventory = lockedResources.sectionById().get(sectionId);
        if (inventory == null) {
            throw new IllegalStateException("show section inventory not found");
        }
        return inventory;
    }

    private Set<Long> seatIdsOf(Reservation reservation) {
        return reservation.getItems().stream()
                .map(ReservationItem::getSeatId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> sectionIdsOf(Reservation reservation) {
        return reservation.getItems().stream()
                .map(ReservationItem::getSectionId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public record LockedResources(
            Map<Long, ShowSeat> seatById,
            Map<Long, ShowSectionInventory> sectionById
    ) {
    }
}