package com.ticketinglab.ticket.presentation;

import com.ticketinglab.ticket.application.ListMyTicketsUseCase;
import com.ticketinglab.ticket.presentation.dto.MyTicketListResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final ListMyTicketsUseCase listMyTicketsUseCase;

    @GetMapping("/api/me/tickets")
    public ResponseEntity<MyTicketListResponse> listMine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        MyTicketListResponse response = listMyTicketsUseCase.execute(
                Long.valueOf(authentication.getName()),
                page,
                size
        );
        return ResponseEntity.ok(response);
    }
}