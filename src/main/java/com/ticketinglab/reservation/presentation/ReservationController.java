package com.ticketinglab.reservation.presentation;

import com.ticketinglab.reservation.application.CreateReservationUseCase;
import com.ticketinglab.reservation.application.GetReservationUseCase;
import com.ticketinglab.reservation.application.ListMyReservationsUseCase;
import com.ticketinglab.reservation.presentation.dto.CreateReservationRequest;
import com.ticketinglab.reservation.presentation.dto.CreateReservationResponse;
import com.ticketinglab.reservation.presentation.dto.MyReservationListResponse;
import com.ticketinglab.reservation.presentation.dto.ReservationDetailResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final GetReservationUseCase getReservationUseCase;
    private final ListMyReservationsUseCase listMyReservationsUseCase;

    @PostMapping("/api/reservations")
    public ResponseEntity<CreateReservationResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        CreateReservationResponse response = createReservationUseCase.execute(
                Long.valueOf(authentication.getName()),
                request
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/reservations/{reservationId}")
    public ResponseEntity<ReservationDetailResponse> detail(
            Authentication authentication,
            @PathVariable String reservationId
    ) {
        ReservationDetailResponse response = getReservationUseCase.execute(
                Long.valueOf(authentication.getName()),
                reservationId
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/me/reservations")
    public ResponseEntity<MyReservationListResponse> listMine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String status
    ) {
        MyReservationListResponse response = listMyReservationsUseCase.execute(
                Long.valueOf(authentication.getName()),
                page,
                size,
                status
        );
        return ResponseEntity.ok(response);
    }
}