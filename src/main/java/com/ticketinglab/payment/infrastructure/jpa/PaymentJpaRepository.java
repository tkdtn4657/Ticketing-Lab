package com.ticketinglab.payment.infrastructure.jpa;

import com.ticketinglab.payment.domain.Payment;
import com.ticketinglab.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = "reservation")
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = "reservation")
    Optional<Payment> findFirstByReservation_IdAndStatus(String reservationId, PaymentStatus status);

    default Optional<Payment> findApprovedByReservationId(String reservationId) {
        return findFirstByReservation_IdAndStatus(reservationId, PaymentStatus.APPROVED);
    }
}