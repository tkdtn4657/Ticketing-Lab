package com.ticketinglab.payment.application;

import com.ticketinglab.payment.domain.Payment;
import com.ticketinglab.payment.domain.PaymentRepository;
import com.ticketinglab.payment.presentation.dto.ConfirmPaymentRequest;
import com.ticketinglab.payment.presentation.dto.ConfirmPaymentResponse;
import com.ticketinglab.reservation.application.ReservationResourceManager;
import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationItem;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.ticket.domain.Ticket;
import com.ticketinglab.ticket.domain.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationResourceManager reservationResourceManager;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public ConfirmPaymentResponse execute(
            Long userId,
            String idempotencyKey,
            ConfirmPaymentRequest request
    ) {
        Payment existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existingPayment != null) {
            validateIdempotentRequest(existingPayment, userId, request);
            return ConfirmPaymentResponse.from(existingPayment);
        }

        Reservation reservation = reservationRepository.findByIdForUpdate(request.reservationId())
                .filter(found -> found.isOwnedBy(userId))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "reservation not found"));

        Payment lockedExistingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (lockedExistingPayment != null) {
            validateIdempotentRequest(lockedExistingPayment, userId, request);
            return ConfirmPaymentResponse.from(lockedExistingPayment);
        }

        LocalDateTime now = LocalDateTime.now();
        reservationResourceManager.expire(reservation, now);
        validateReservation(reservation, request.amount());

        if (paymentRepository.findApprovedByReservationId(reservation.getId()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "reservation already paid");
        }

        ReservationResourceManager.LockedResources lockedResources = reservationResourceManager.lockResources(
                reservation.getShowId(),
                seatIdsOf(reservation)
        );

        validateLockedSeats(reservation, lockedResources);
        reservation.confirmPayment();

        Payment savedPayment = paymentRepository.save(
                Payment.approve(reservation, idempotencyKey, request.amount(), null)
        );
        ticketRepository.saveAll(issueTickets(reservation));

        return ConfirmPaymentResponse.from(savedPayment);
    }

    private void validateIdempotentRequest(
            Payment payment,
            Long userId,
            ConfirmPaymentRequest request
    ) {
        if (!payment.matches(userId, request.reservationId(), request.amount())) {
            throw new ResponseStatusException(CONFLICT, "idempotency key already used");
        }
    }

    private void validateReservation(Reservation reservation, int amount) {
        if (!reservation.isPendingPayment()) {
            throw new ResponseStatusException(
                    CONFLICT,
                    reservation.isPaid() ? "reservation already paid" : "reservation not pending payment"
            );
        }
        if (!reservation.hasAmount(amount)) {
            throw new ResponseStatusException(CONFLICT, "reservation amount mismatch");
        }
    }

    private void validateLockedSeats(
            Reservation reservation,
            ReservationResourceManager.LockedResources lockedResources
    ) {
        for (ReservationItem item : reservation.getItems()) {
            if (!lockedResources.seatById().containsKey(item.getSeatId())) {
                throw new IllegalStateException("show seat not found");
            }
        }
    }

    private List<Ticket> issueTickets(Reservation reservation) {
        return reservation.getItems().stream()
                .map(Ticket::issue)
                .toList();
    }

    private Set<Long> seatIdsOf(Reservation reservation) {
        return reservation.getItems().stream()
                .map(ReservationItem::getSeatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

}
