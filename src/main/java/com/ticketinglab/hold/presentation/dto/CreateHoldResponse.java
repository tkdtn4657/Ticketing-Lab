package com.ticketinglab.hold.presentation.dto;

import java.time.LocalDateTime;

public record CreateHoldResponse(
        String holdId,
        LocalDateTime expiresAt
) {
}
