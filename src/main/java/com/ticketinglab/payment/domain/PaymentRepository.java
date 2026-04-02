package com.ticketinglab.payment.domain;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findApprovedByReservationId(String reservationId);
}