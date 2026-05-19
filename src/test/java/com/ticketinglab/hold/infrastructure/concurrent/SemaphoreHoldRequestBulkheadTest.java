package com.ticketinglab.hold.infrastructure.concurrent;

import com.ticketinglab.hold.application.HoldRequestPermit;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SemaphoreHoldRequestBulkheadTest {

    @Test
    @DisplayName("동시 처리 슬롯이 꽉 차면 permit 획득에 실패한다")
    void tryAcquire_rejectsWhenPermitsAreExhausted() {
        SemaphoreHoldRequestBulkhead bulkhead = new SemaphoreHoldRequestBulkhead(
                true,
                2,
                new SimpleMeterRegistry()
        );

        Optional<HoldRequestPermit> first = bulkhead.tryAcquire();
        Optional<HoldRequestPermit> second = bulkhead.tryAcquire();
        Optional<HoldRequestPermit> rejected = bulkhead.tryAcquire();

        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(rejected).isEmpty();

        first.orElseThrow().close();
        Optional<HoldRequestPermit> acquiredAfterRelease = bulkhead.tryAcquire();

        assertThat(acquiredAfterRelease).isPresent();

        second.orElseThrow().close();
        acquiredAfterRelease.orElseThrow().close();
    }

    @Test
    @DisplayName("fast-fail이 꺼져 있으면 항상 permit을 반환한다")
    void tryAcquire_permitsAllWhenDisabled() {
        SemaphoreHoldRequestBulkhead bulkhead = new SemaphoreHoldRequestBulkhead(
                false,
                1,
                new SimpleMeterRegistry()
        );

        HoldRequestPermit first = bulkhead.tryAcquire().orElseThrow();
        HoldRequestPermit second = bulkhead.tryAcquire().orElseThrow();

        first.close();
        second.close();
    }
}
