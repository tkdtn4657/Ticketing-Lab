package com.ticketinglab.hold.presentation.dto;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldItem;

import java.util.Comparator;
import java.util.List;

public record HoldDetailResponse(
        HoldSummaryResponse hold,
        List<HoldItemResponse> items
) {
    private static final Comparator<HoldItem> ITEM_ORDER = Comparator
            .comparing(HoldItem::getType)
            .thenComparing(HoldItem::getSeatId, Comparator.nullsLast(Long::compareTo))
            .thenComparing(HoldItem::getSectionId, Comparator.nullsLast(Long::compareTo));

    public static HoldDetailResponse from(Hold hold) {
        List<HoldItemResponse> items = hold.getItems().stream()
                .sorted(ITEM_ORDER)
                .map(HoldItemResponse::from)
                .toList();

        return new HoldDetailResponse(HoldSummaryResponse.from(hold), items);
    }
}
