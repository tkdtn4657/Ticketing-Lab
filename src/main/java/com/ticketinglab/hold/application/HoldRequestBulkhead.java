package com.ticketinglab.hold.application;

import java.util.Optional;

public interface HoldRequestBulkhead {

    Optional<HoldRequestPermit> tryAcquire();
}
