package com.ticketinglab.expiration.infrastructure;

import com.ticketinglab.expiration.application.ExpiredResourceCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.expiration.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class ExpiredResourceCleanupScheduler {

    private final ExpiredResourceCleanupService cleanupService;

    @Scheduled(fixedDelayString = "${app.expiration.cleanup.fixed-delay-ms:30000}")
    public void cleanupExpiredResources() {
        log.info(
                "Expired resource cleanup start"
        );
        var result = cleanupService.cleanupAll();
        if (result.totalExpired() > 0) {
            log.info(
                    "Expired resource cleanup completed. holds={}, reservations={}",
                    result.expiredHolds(),
                    result.expiredReservations()
            );
        }
    }
}
