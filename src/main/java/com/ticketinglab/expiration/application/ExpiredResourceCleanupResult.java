package com.ticketinglab.expiration.application;

public record ExpiredResourceCleanupResult(
        int expiredHolds,
        int expiredReservations
) {
    public int totalExpired() {
        return expiredHolds + expiredReservations;
    }
}
