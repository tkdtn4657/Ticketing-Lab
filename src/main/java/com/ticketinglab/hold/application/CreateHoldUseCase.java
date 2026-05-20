package com.ticketinglab.hold.application;

import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.hold.presentation.dto.CreateHoldRequest;
import com.ticketinglab.hold.presentation.dto.CreateHoldResponse;
import com.ticketinglab.reservation.application.ReservationResourceManager;
import com.ticketinglab.show.domain.ShowSeat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateHoldUseCase {

    private final ShowRepository showRepository;
    private final HoldRepository holdRepository;
    private final HoldResourceManager holdResourceManager;
    private final ReservationResourceManager reservationResourceManager;
    private final SeatHoldPreLockManager seatHoldPreLockManager;
    private final SeatHoldQueueManager seatHoldQueueManager;

    @Value("${app.hold.ttl-minutes:5}")
    private long holdTtlMinutes;

    @Value("${app.hold.pre-lock.ttl:60s}")
    private Duration preLockTtl;

    @Value("${app.hold.seat-queue.ttl:30s}")
    private Duration seatQueueTtl;

    @Transactional
    public CreateHoldResponse execute(Long userId, CreateHoldRequest request) {
        RequestedItems requestedItems = normalize(request.items());
        SeatHoldQueueTicket queueTicket = enterSeatQueue(request.showId(), requestedItems.seatIds(), userId);

        try {
            return createHold(userId, request.showId(), requestedItems);
        } finally {
            leaveSeatQueue(queueTicket);
        }
    }

    private CreateHoldResponse createHold(Long userId, Long showId, RequestedItems requestedItems) {
        LocalDateTime now = LocalDateTime.now();
        SeatHoldPreLock preLock = acquirePreLock(showId, requestedItems.seatIds());
        boolean preLockHandledByTransaction = false;

        try {
            Show show = showRepository.findById(showId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "show not found"));

            reservationResourceManager.expirePendingReservations(
                    show.getId(),
                    requestedItems.seatIds(),
                    now
            );

            HoldResourceManager.LockedResources lockedResources = holdResourceManager.prepareForCreate(
                    show.getId(),
                    requestedItems.seatIds(),
                    now
            );

            validateResourceExistence(requestedItems, lockedResources);

            Hold hold = Hold.create(userId, show.getId(), now.plusMinutes(holdTtlMinutes));
            applyHoldItems(hold, requestedItems.items(), lockedResources);

            Hold savedHold = holdRepository.save(hold);
            confirmPreLockAfterTransaction(preLock, savedHold.getId());
            preLockHandledByTransaction = true;
            return new CreateHoldResponse(savedHold.getId(), savedHold.getExpiresAt());
        } catch (OptimisticLockingFailureException exception) {
            releasePreLockIfNeeded(preLock, preLockHandledByTransaction);
            throw new ResponseStatusException(CONFLICT, "seat not available", exception);
        } catch (RuntimeException exception) {
            releasePreLockIfNeeded(preLock, preLockHandledByTransaction);
            throw exception;
        }
    }

    private SeatHoldPreLock acquirePreLock(Long showId, Set<Long> seatIds) {
        try {
            return seatHoldPreLockManager.acquire(showId, seatIds, preLockTtl)
                    .orElseThrow(() -> new ResponseStatusException(CONFLICT, "seat not available"));
        } catch (DataAccessException exception) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "seat pre-lock unavailable", exception);
        }
    }

    private void confirmPreLockAfterTransaction(SeatHoldPreLock preLock, String holdId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            confirmPreLock(preLock, holdId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                confirmPreLock(preLock, holdId);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    releasePreLock(preLock);
                }
            }
        });
    }

    private void confirmPreLock(SeatHoldPreLock preLock, String holdId) {
        try {
            seatHoldPreLockManager.confirmHold(preLock, holdId, preLockTtl);
        } catch (DataAccessException exception) {
            log.warn("failed to confirm seat hold pre-lock. holdId={}", holdId, exception);
        }
    }

    private void releasePreLockIfNeeded(SeatHoldPreLock preLock, boolean handledByTransaction) {
        if (!handledByTransaction) {
            releasePreLock(preLock);
        }
    }

    private void releasePreLock(SeatHoldPreLock preLock) {
        try {
            seatHoldPreLockManager.release(preLock);
        } catch (DataAccessException exception) {
            log.warn("failed to release seat hold pre-lock. showId={}, seatIds={}",
                    preLock.showId(),
                    preLock.seatIds(),
                    exception
            );
        }
    }

    private SeatHoldQueueTicket enterSeatQueue(Long showId, Set<Long> seatIds, Long userId) {
        try {
            return seatHoldQueueManager.tryEnter(showId, seatIds, userId, seatQueueTtl)
                    .orElseThrow(() -> new ResponseStatusException(CONFLICT, "seat request queue full"));
        } catch (DataAccessException exception) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "seat request queue unavailable", exception);
        }
    }

    private void leaveSeatQueue(SeatHoldQueueTicket queueTicket) {
        try {
            seatHoldQueueManager.leave(queueTicket);
        } catch (DataAccessException ignored) {
        }
    }

    private RequestedItems normalize(List<CreateHoldRequest.Item> items) {
        List<RequestedItem> normalizedItems = new ArrayList<>();
        Set<Long> seatIds = new LinkedHashSet<>();

        for (CreateHoldRequest.Item item : items) {
            if (!seatIds.add(item.seatId())) {
                throw new ResponseStatusException(CONFLICT, "duplicate seat ids");
            }
            normalizedItems.add(RequestedItem.seat(item.seatId()));
        }

        return new RequestedItems(normalizedItems, seatIds);
    }

    private void validateResourceExistence(
            RequestedItems requestedItems,
            HoldResourceManager.LockedResources lockedResources
    ) {
        if (lockedResources.seatById().size() != requestedItems.seatIds().size()) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid seat ids");
        }
    }

    private void applyHoldItems(
            Hold hold,
            List<RequestedItem> requestedItems,
            HoldResourceManager.LockedResources lockedResources
    ) {
        for (RequestedItem requestedItem : requestedItems) {
            ShowSeat showSeat = lockedResources.seatById().get(requestedItem.seatId());
            try {
                showSeat.hold();
            } catch (IllegalStateException exception) {
                throw new ResponseStatusException(CONFLICT, exception.getMessage());
            }
            hold.addSeatItem(requestedItem.seatId(), showSeat.getPrice());
        }
    }

    private record RequestedItems(
            List<RequestedItem> items,
            Set<Long> seatIds
    ) {
    }

    private record RequestedItem(
            Long seatId
    ) {
        static RequestedItem seat(Long seatId) {
            return new RequestedItem(seatId);
        }

    }
}
