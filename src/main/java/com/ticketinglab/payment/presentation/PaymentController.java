package com.ticketinglab.payment.presentation;

import com.ticketinglab.payment.application.ConfirmPaymentUseCase;
import com.ticketinglab.payment.presentation.dto.ConfirmPaymentRequest;
import com.ticketinglab.payment.presentation.dto.ConfirmPaymentResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @PostMapping("/confirm")
    public ResponseEntity<ConfirmPaymentResponse> confirm(
            Authentication authentication,
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String idempotencyKey,
            @Valid @RequestBody ConfirmPaymentRequest request
    ) {
        ConfirmPaymentResponse response = confirmPaymentUseCase.execute(
                Long.valueOf(authentication.getName()),
                idempotencyKey,
                request
        );
        return ResponseEntity.ok(response);
    }
}