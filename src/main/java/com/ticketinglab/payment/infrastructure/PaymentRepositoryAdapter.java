package com.ticketinglab.payment.infrastructure;

import com.ticketinglab.payment.domain.Payment;
import com.ticketinglab.payment.domain.PaymentRepository;
import com.ticketinglab.payment.infrastructure.jpa.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Optional<Payment> findApprovedByReservationId(String reservationId) {
        return jpaRepository.findApprovedByReservationId(reservationId);
    }
}