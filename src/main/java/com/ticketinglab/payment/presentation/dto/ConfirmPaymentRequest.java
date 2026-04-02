package com.ticketinglab.payment.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ConfirmPaymentRequest(
        @NotBlank String reservationId,
        @Positive int amount
) {
}