package com.ticketinglab.hold.presentation.dto;

import com.ticketinglab.hold.domain.HoldItem;

public record HoldItemResponse(
        String type,
        Long seatId,
        Long sectionId,
        int qty,
        int unitPrice
) {
    public static HoldItemResponse from(HoldItem holdItem) {
        return new HoldItemResponse(
                holdItem.getType().name(),
                holdItem.getSeatId(),
                holdItem.getSectionId(),
                holdItem.getQty(),
                holdItem.getUnitPrice()
        );
    }
}
