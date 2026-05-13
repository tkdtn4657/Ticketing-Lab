package com.ticketinglab.venue.domain;

public enum SectionSaleType {
    ASSIGNED_SEAT,
    GENERAL_ADMISSION;

    public static SectionSaleType from(String value) {
        if (value == null || value.isBlank()) {
            return GENERAL_ADMISSION;
        }
        return SectionSaleType.valueOf(value.trim().toUpperCase());
    }
}
