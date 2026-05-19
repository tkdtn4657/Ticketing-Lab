package com.ticketinglab.hold.infrastructure.concurrent;

import com.ticketinglab.hold.application.HoldRequestBulkhead;
import com.ticketinglab.hold.application.HoldRequestPermit;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.Semaphore;

@Component
public class SemaphoreHoldRequestBulkhead implements HoldRequestBulkhead {

    private final boolean enabled;
    private final int maxConcurrent;
    private final Semaphore semaphore;
    private final Counter rejectedRequests;

    public SemaphoreHoldRequestBulkhead(
            @Value("${app.hold.fast-fail.enabled:false}") boolean enabled,
            @Value("${app.hold.fast-fail.max-concurrent:50}") int maxConcurrent,
            MeterRegistry meterRegistry
    ) {
        this.enabled = enabled;
        this.maxConcurrent = Math.max(maxConcurrent, 1);
        this.semaphore = new Semaphore(this.maxConcurrent);
        this.rejectedRequests = Counter.builder("ticketing.hold.fast.fail.rejected")
                .description("Hold create requests rejected by fast-fail bulkhead")
                .register(meterRegistry);

        Gauge.builder("ticketing.hold.fast.fail.available", semaphore, Semaphore::availablePermits)
                .description("Available Hold fast-fail bulkhead permits")
                .register(meterRegistry);
        Gauge.builder("ticketing.hold.fast.fail.in.use", this, SemaphoreHoldRequestBulkhead::inUse)
                .description("In-use Hold fast-fail bulkhead permits")
                .register(meterRegistry);
    }

    @Override
    public Optional<HoldRequestPermit> tryAcquire() {
        if (!enabled) {
            return Optional.of(HoldRequestPermit.noop());
        }
        if (!semaphore.tryAcquire()) {
            rejectedRequests.increment();
            return Optional.empty();
        }
        return Optional.of(HoldRequestPermit.acquired(semaphore::release));
    }

    private int inUse() {
        return maxConcurrent - semaphore.availablePermits();
    }
}
