package com.ticketinglab.reservation.application;

import com.ticketinglab.hold.application.HoldResourceManager;
import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldItem;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.reservation.presentation.dto.CreateReservationRequest;
import com.ticketinglab.reservation.presentation.dto.CreateReservationResponse;
import com.ticketinglab.show.domain.ShowSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private final HoldRepository holdRepository;
    private final ReservationRepository reservationRepository;
    private final HoldResourceManager holdResourceManager;
    private final ReservationResourceManager reservationResourceManager;

    @Value("${app.reservation.ttl-minutes:15}")
    private long reservationTtlMinutes;

    @Transactional
    public CreateReservationResponse execute(Long userId, CreateReservationRequest request) {
        Hold hold = holdRepository.findByIdForUpdate(request.holdId())
                .filter(found -> found.isOwnedBy(userId))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "hold not found"));

        LocalDateTime now = LocalDateTime.now();
        holdResourceManager.expire(hold, now);

        Reservation reservation;
        try {
            ReservationResourceManager.LockedResources lockedResources = reservationResourceManager.lockResources(
                    hold.getShowId(),
                    seatIdsOf(hold),
                    sectionIdsOf(hold)
            );

            reserveSeats(hold, lockedResources);
            hold.convert();
            reservation = Reservation.createFromHold(hold, now.plusMinutes(reservationTtlMinutes));
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }

        holdRepository.save(hold);
        Reservation savedReservation = reservationRepository.save(reservation);
        return new CreateReservationResponse(savedReservation.getId(), savedReservation.getStatus().name());
    }

    private void reserveSeats(Hold hold, ReservationResourceManager.LockedResources lockedResources) {
        for (HoldItem item : hold.getItems()) {
            if (item.getSeatId() == null) {
                continue;
            }
            ShowSeat showSeat = lockedResources.seatById().get(item.getSeatId());
            if (showSeat == null) {
                throw new IllegalStateException("show seat not found");
            }
            showSeat.reserve();
        }
    }

    private Set<Long> seatIdsOf(Hold hold) {
        return hold.getItems().stream()
                .map(HoldItem::getSeatId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> sectionIdsOf(Hold hold) {
        return hold.getItems().stream()
                .map(HoldItem::getSectionId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }
}