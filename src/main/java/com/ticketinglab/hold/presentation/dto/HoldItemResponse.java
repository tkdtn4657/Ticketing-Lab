package com.ticketinglab.hold.presentation.dto;

import com.ticketinglab.hold.domain.HoldItem;

public record HoldItemResponse(
        String type,
        Long seatId,
        int unitPrice
) {
    public static HoldItemResponse from(HoldItem holdItem) {
        return new HoldItemResponse(
                holdItem.getType().name(),
                holdItem.getSeatId(),
                holdItem.getUnitPrice()
        );
    }
}
