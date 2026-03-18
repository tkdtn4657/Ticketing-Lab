package com.ticketinglab.event.domain;

import java.util.Arrays;

public enum EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED;

    public static EventStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid event status"));
    }
}
