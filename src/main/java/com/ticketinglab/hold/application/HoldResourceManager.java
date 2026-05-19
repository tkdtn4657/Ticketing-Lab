package com.ticketinglab.hold.application;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldItem;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class HoldResourceManager {

    private final HoldRepository holdRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatHoldPreLockManager seatHoldPreLockManager;

    public int expireActiveHolds(LocalDateTime now, int limit) {
        return expireActiveHoldIds(holdRepository.findActiveExpiredIds(now, limit), now);
    }

    public int expireActiveHoldsByShowId(Long showId, LocalDateTime now, int limit) {
        return expireActiveHoldIds(holdRepository.findActiveExpiredIdsByShowId(showId, now, limit), now);
    }

    public LockedResources prepareForCreate(
            Long showId,
            Collection<Long> seatIds,
            LocalDateTime now
    ) {
        LockedResources lockedResources = lockResources(showId, seatIds);
        Map<String, Hold> expiredHolds = findExpiredHolds(showId, seatIds, now);

        if (expiredHolds.isEmpty()) {
            return lockedResources;
        }

        Set<Long> allSeatIds = new LinkedHashSet<>(seatIds);

        expiredHolds.values().stream()
                .flatMap(hold -> hold.getItems().stream())
                .forEach(item -> collectResourceIds(item, allSeatIds));

        LockedResources releasableResources = allSeatIds.size() == seatIds.size()
                ? lockedResources
                : lockResources(showId, allSeatIds);

        expiredHolds.values().forEach(hold -> expire(hold, now, releasableResources));
        return releasableResources;
    }

    public void expire(Hold hold, LocalDateTime now) {
        if (!hold.isActive() || !hold.isExpiredAt(now)) {
            return;
        }

        LockedResources lockedResources = lockResources(
                hold.getShowId(),
                seatIdsOf(hold)
        );
        expire(hold, now, lockedResources);
    }

    public void cancel(Hold hold) {
        if (!hold.isActive()) {
            hold.cancel();
            return;
        }

        LockedResources lockedResources = lockResources(
                hold.getShowId(),
                seatIdsOf(hold)
        );

        hold.cancel();
        releaseResources(hold, lockedResources);
        holdRepository.save(hold);
        releaseSeatPreLocksAfterCommit(hold);
    }

    private void expire(Hold hold, LocalDateTime now, LockedResources lockedResources) {
        if (!hold.isActive() || !hold.isExpiredAt(now)) {
            return;
        }

        hold.expire(now);
        releaseResources(hold, lockedResources);
        holdRepository.save(hold);
        releaseSeatPreLocksAfterCommit(hold);
    }

    public void releaseSeatPreLocksAfterCommit(Hold hold) {
        Set<Long> seatIds = seatIdsOf(hold);
        if (seatIds.isEmpty()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            releaseSeatPreLocks(hold.getShowId(), seatIds, hold.getId());
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                releaseSeatPreLocks(hold.getShowId(), seatIds, hold.getId());
            }
        });
    }

    private void releaseSeatPreLocks(Long showId, Collection<Long> seatIds, String holdId) {
        try {
            seatHoldPreLockManager.releaseHold(showId, seatIds, holdId);
        } catch (DataAccessException exception) {
            log.warn("failed to release confirmed seat hold pre-lock. holdId={}", holdId, exception);
        }
    }

    private int expireActiveHoldIds(Collection<String> holdIds, LocalDateTime now) {
        int expiredCount = 0;
        for (String holdId : holdIds) {
            Hold hold = holdRepository.findByIdForUpdate(holdId).orElse(null);
            if (hold == null || !hold.isActive() || !hold.isExpiredAt(now)) {
                continue;
            }
            expire(hold, now);
            expiredCount++;
        }
        return expiredCount;
    }

    private Map<String, Hold> findExpiredHolds(
            Long showId,
            Collection<Long> seatIds,
            LocalDateTime now
    ) {
        Map<String, Hold> expiredHolds = new LinkedHashMap<>();

        if (!seatIds.isEmpty()) {
            holdRepository.findAllActiveExpiredByShowIdAndSeatIdIn(showId, seatIds, now)
                    .forEach(hold -> expiredHolds.put(hold.getId(), hold));
        }

        return expiredHolds;
    }

    private LockedResources lockResources(
            Long showId,
            Collection<Long> seatIds
    ) {
        Map<Long, ShowSeat> seatById = seatIds.isEmpty()
                ? Map.of()
                : showSeatRepository.findAllByShowIdAndSeatIdInForUpdate(showId, seatIds).stream()
                .collect(Collectors.toMap(showSeat -> showSeat.getSeat().getId(), Function.identity()));

        return new LockedResources(seatById);
    }

    private void releaseResources(Hold hold, LockedResources lockedResources) {
        for (HoldItem item : hold.getItems()) {
            if (item.getSeatId() != null) {
                requiredSeat(lockedResources, item.getSeatId()).releaseHold();
            }
        }
    }

    private ShowSeat requiredSeat(LockedResources lockedResources, Long seatId) {
        ShowSeat showSeat = lockedResources.seatById().get(seatId);
        if (showSeat == null) {
            throw new IllegalStateException("show seat not found");
        }
        return showSeat;
    }

    private Set<Long> seatIdsOf(Hold hold) {
        return hold.getItems().stream()
                .map(HoldItem::getSeatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void collectResourceIds(HoldItem item, Set<Long> seatIds) {
        if (item.getSeatId() != null) {
            seatIds.add(item.getSeatId());
        }
    }

    public record LockedResources(
            Map<Long, ShowSeat> seatById
    ) {
    }
}
