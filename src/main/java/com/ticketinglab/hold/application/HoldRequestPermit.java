package com.ticketinglab.hold.application;

import java.util.concurrent.atomic.AtomicBoolean;

public final class HoldRequestPermit implements AutoCloseable {

    private static final HoldRequestPermit NOOP = new HoldRequestPermit(() -> {
    });

    private final Runnable release;
    private final AtomicBoolean closed = new AtomicBoolean();

    private HoldRequestPermit(Runnable release) {
        this.release = release;
    }

    public static HoldRequestPermit acquired(Runnable release) {
        return new HoldRequestPermit(release);
    }

    public static HoldRequestPermit noop() {
        return NOOP;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            release.run();
        }
    }
}
