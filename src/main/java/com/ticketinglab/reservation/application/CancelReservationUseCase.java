package com.ticketinglab.reservation.application;

import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CancelReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationResourceManager reservationResourceManager;

    @Transactional
    public void execute(Long userId, String reservationId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .filter(found -> found.isOwnedBy(userId))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "reservation not found"));

        reservationResourceManager.expire(reservation, LocalDateTime.now());
        try {
            reservationResourceManager.cancel(reservation);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }
    }
}
