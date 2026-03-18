package com.ticketinglab.show.presentation.dto;

import com.ticketinglab.show.domain.ShowSectionInventory;

public record SectionAvailabilityResponse(
        Long sectionId,
        String name,
        int price,
        int remainingQty
) {
    public static SectionAvailabilityResponse from(ShowSectionInventory inventory) {
        return new SectionAvailabilityResponse(
                inventory.getSection().getId(),
                inventory.getSection().getName(),
                inventory.getPrice(),
                inventory.remainingQuantity()
        );
    }
}
