package com.ticketinglab.expiration.application;

import com.ticketinglab.hold.application.HoldResourceManager;
import com.ticketinglab.reservation.application.ReservationResourceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExpiredResourceCleanupService {

    private final HoldResourceManager holdResourceManager;
    private final ReservationResourceManager reservationResourceManager;

    @Value("${app.expiration.cleanup.batch-size:200}")
    private int batchSize;

    @Transactional
    public ExpiredResourceCleanupResult cleanupShow(Long showId) {
        LocalDateTime now = LocalDateTime.now();
        int expiredReservations = reservationResourceManager.expirePendingReservationsByShowId(showId, now, batchSize);
        int expiredHolds = holdResourceManager.expireActiveHoldsByShowId(showId, now, batchSize);
        return new ExpiredResourceCleanupResult(expiredHolds, expiredReservations);
    }

    @Transactional
    public ExpiredResourceCleanupResult cleanupAll() {
        LocalDateTime now = LocalDateTime.now();
        int expiredReservations = reservationResourceManager.expirePendingReservations(now, batchSize);
        int expiredHolds = holdResourceManager.expireActiveHolds(now, batchSize);
        return new ExpiredResourceCleanupResult(expiredHolds, expiredReservations);
    }
}
