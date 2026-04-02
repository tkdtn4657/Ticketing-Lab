package com.ticketinglab.payment.presentation.dto;

import com.ticketinglab.payment.domain.Payment;

import java.time.LocalDateTime;

public record ConfirmPaymentResponse(
        Long paymentId,
        String reservationId,
        String status,
        String reservationStatus,
        LocalDateTime approvedAt
) {
    public static ConfirmPaymentResponse from(Payment payment) {
        return new ConfirmPaymentResponse(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getStatus().name(),
                payment.getReservation().getStatus().name(),
                payment.getApprovedAt()
        );
    }
}