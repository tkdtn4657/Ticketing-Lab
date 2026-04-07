package com.ticketinglab.payment.presentation;

import com.ticketinglab.config.openapi.OpenApiExamples;
import com.ticketinglab.payment.application.ConfirmPaymentUseCase;
import com.ticketinglab.payment.presentation.dto.ConfirmPaymentRequest;
import com.ticketinglab.payment.presentation.dto.ConfirmPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@Tag(name = "Payment")
public class PaymentController {

    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @Operation(summary = "결제 승인", description = "PAY-001. Idempotency-Key와 예약 금액을 검증한 뒤 결제를 승인하고 티켓을 발급합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "결제 승인 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ConfirmPaymentRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.PAYMENT_CONFIRM_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 승인 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConfirmPaymentResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.PAYMENT_CONFIRM_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 형식 또는 Idempotency-Key가 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "이미 결제된 예약이거나 요청 금액이 일치하지 않습니다.")
    })
    @PostMapping("/confirm")
    public ResponseEntity<ConfirmPaymentResponse> confirm(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "멱등성 키", example = OpenApiExamples.IDEMPOTENCY_KEY)
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