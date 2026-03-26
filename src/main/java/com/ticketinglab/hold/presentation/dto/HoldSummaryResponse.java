package com.ticketinglab.hold.presentation.dto;

import com.ticketinglab.hold.domain.Hold;

import java.time.LocalDateTime;

public record HoldSummaryResponse(
        String holdId,
        Long showId,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static HoldSummaryResponse from(Hold hold) {
        return new HoldSummaryResponse(
                hold.getId(),
                hold.getShowId(),
                hold.getStatus().name(),
                hold.getExpiresAt(),
                hold.getCreatedAt()
        );
    }
}
