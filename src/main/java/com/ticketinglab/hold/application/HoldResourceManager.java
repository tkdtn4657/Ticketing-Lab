package com.ticketinglab.hold.application;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldItem;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class HoldResourceManager {

    private final HoldRepository holdRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowSectionInventoryRepository showSectionInventoryRepository;

    public LockedResources prepareForCreate(
            Long showId,
            Collection<Long> seatIds,
            Collection<Long> sectionIds,
            LocalDateTime now
    ) {
        LockedResources lockedResources = lockResources(showId, seatIds, sectionIds);
        Map<String, Hold> expiredHolds = findExpiredHolds(showId, seatIds, sectionIds, now);

        if (expiredHolds.isEmpty()) {
            return lockedResources;
        }

        Set<Long> allSeatIds = new LinkedHashSet<>(seatIds);
        Set<Long> allSectionIds = new LinkedHashSet<>(sectionIds);

        expiredHolds.values().stream()
                .flatMap(hold -> hold.getItems().stream())
                .forEach(item -> collectResourceIds(item, allSeatIds, allSectionIds));

        LockedResources releasableResources = allSeatIds.size() == seatIds.size()
                && allSectionIds.size() == sectionIds.size()
                ? lockedResources
                : lockResources(showId, allSeatIds, allSectionIds);

        expiredHolds.values().forEach(hold -> expire(hold, now, releasableResources));
        return releasableResources;
    }

    public void expire(Hold hold, LocalDateTime now) {
        if (!hold.isActive() || !hold.isExpiredAt(now)) {
            return;
        }

        LockedResources lockedResources = lockResources(
                hold.getShowId(),
                seatIdsOf(hold),
                sectionIdsOf(hold)
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
                seatIdsOf(hold),
                sectionIdsOf(hold)
        );

        hold.cancel();
        releaseResources(hold, lockedResources);
        holdRepository.save(hold);
    }

    private void expire(Hold hold, LocalDateTime now, LockedResources lockedResources) {
        if (!hold.isActive() || !hold.isExpiredAt(now)) {
            return;
        }

        hold.expire(now);
        releaseResources(hold, lockedResources);
        holdRepository.save(hold);
    }

    private Map<String, Hold> findExpiredHolds(
            Long showId,
            Collection<Long> seatIds,
            Collection<Long> sectionIds,
            LocalDateTime now
    ) {
        Map<String, Hold> expiredHolds = new LinkedHashMap<>();

        if (!seatIds.isEmpty()) {
            holdRepository.findAllActiveExpiredByShowIdAndSeatIdIn(showId, seatIds, now)
                    .forEach(hold -> expiredHolds.put(hold.getId(), hold));
        }

        if (!sectionIds.isEmpty()) {
            holdRepository.findAllActiveExpiredByShowIdAndSectionIdIn(showId, sectionIds, now)
                    .forEach(hold -> expiredHolds.put(hold.getId(), hold));
        }

        return expiredHolds;
    }

    private LockedResources lockResources(
            Long showId,
            Collection<Long> seatIds,
            Collection<Long> sectionIds
    ) {
        Map<Long, ShowSeat> seatById = seatIds.isEmpty()
                ? Map.of()
                : showSeatRepository.findAllByShowIdAndSeatIdInForUpdate(showId, seatIds).stream()
                .collect(Collectors.toMap(showSeat -> showSeat.getSeat().getId(), Function.identity()));

        Map<Long, ShowSectionInventory> sectionById = sectionIds.isEmpty()
                ? Map.of()
                : showSectionInventoryRepository.findAllByShowIdAndSectionIdInForUpdate(showId, sectionIds).stream()
                .collect(Collectors.toMap(inventory -> inventory.getSection().getId(), Function.identity()));

        return new LockedResources(seatById, sectionById);
    }

    private void releaseResources(Hold hold, LockedResources lockedResources) {
        for (HoldItem item : hold.getItems()) {
            if (item.getSeatId() != null) {
                requiredSeat(lockedResources, item.getSeatId()).releaseHold();
            }
            if (item.getSectionId() != null) {
                requiredSection(lockedResources, item.getSectionId()).releaseHold(item.getQty());
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

    private ShowSectionInventory requiredSection(LockedResources lockedResources, Long sectionId) {
        ShowSectionInventory inventory = lockedResources.sectionById().get(sectionId);
        if (inventory == null) {
            throw new IllegalStateException("show section inventory not found");
        }
        return inventory;
    }

    private Set<Long> seatIdsOf(Hold hold) {
        return hold.getItems().stream()
                .map(HoldItem::getSeatId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> sectionIdsOf(Hold hold) {
        return hold.getItems().stream()
                .map(HoldItem::getSectionId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void collectResourceIds(HoldItem item, Set<Long> seatIds, Set<Long> sectionIds) {
        if (item.getSeatId() != null) {
            seatIds.add(item.getSeatId());
        }
        if (item.getSectionId() != null) {
            sectionIds.add(item.getSectionId());
        }
    }

    public record LockedResources(
            Map<Long, ShowSeat> seatById,
            Map<Long, ShowSectionInventory> sectionById
    ) {
    }
}
