package com.ticketinglab.reservation.domain;

import java.util.Arrays;

public enum ReservationStatus {
    PENDING_PAYMENT,
    PAID,
    CANCELED,
    EXPIRED;

    public static ReservationStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid reservation status"));
    }
}