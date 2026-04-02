package com.ticketinglab.hold.application;

import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.hold.presentation.dto.CreateHoldRequest;
import com.ticketinglab.hold.presentation.dto.CreateHoldResponse;
import com.ticketinglab.reservation.application.ReservationResourceManager;
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CreateHoldUseCase {

    private final ShowRepository showRepository;
    private final HoldRepository holdRepository;
    private final HoldResourceManager holdResourceManager;
    private final ReservationResourceManager reservationResourceManager;

    @Value("${app.hold.ttl-minutes:5}")
    private long holdTtlMinutes;

    @Transactional
    public CreateHoldResponse execute(Long userId, CreateHoldRequest request) {
        Show show = showRepository.findById(request.showId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "show not found"));

        RequestedItems requestedItems = normalize(request.items());
        LocalDateTime now = LocalDateTime.now();

        reservationResourceManager.expirePendingReservations(
                show.getId(),
                requestedItems.seatIds(),
                requestedItems.sectionIds(),
                now
        );

        HoldResourceManager.LockedResources lockedResources = holdResourceManager.prepareForCreate(
                show.getId(),
                requestedItems.seatIds(),
                requestedItems.sectionIds(),
                now
        );

        validateResourceExistence(requestedItems, lockedResources);

        Hold hold = Hold.create(userId, show.getId(), now.plusMinutes(holdTtlMinutes));
        applyHoldItems(hold, requestedItems.items(), lockedResources);

        Hold savedHold = holdRepository.save(hold);
        return new CreateHoldResponse(savedHold.getId(), savedHold.getExpiresAt());
    }

    private RequestedItems normalize(List<CreateHoldRequest.Item> items) {
        List<RequestedItem> normalizedItems = new ArrayList<>();
        Set<Long> seatIds = new LinkedHashSet<>();
        Set<Long> sectionIds = new LinkedHashSet<>();

        for (CreateHoldRequest.Item item : items) {
            boolean hasSeat = item.seatId() != null;
            boolean hasSection = item.sectionId() != null;

            if (hasSeat == hasSection) {
                throw new ResponseStatusException(BAD_REQUEST, "invalid hold items");
            }

            if (hasSeat) {
                int quantity = item.qty() == null ? 1 : item.qty();
                if (quantity != 1) {
                    throw new ResponseStatusException(BAD_REQUEST, "seat quantity must be 1");
                }
                if (!seatIds.add(item.seatId())) {
                    throw new ResponseStatusException(CONFLICT, "duplicate seat ids");
                }
                normalizedItems.add(RequestedItem.seat(item.seatId()));
                continue;
            }

            if (item.qty() == null) {
                throw new ResponseStatusException(BAD_REQUEST, "section quantity required");
            }
            if (!sectionIds.add(item.sectionId())) {
                throw new ResponseStatusException(CONFLICT, "duplicate section ids");
            }
            normalizedItems.add(RequestedItem.section(item.sectionId(), item.qty()));
        }

        return new RequestedItems(normalizedItems, seatIds, sectionIds);
    }

    private void validateResourceExistence(
            RequestedItems requestedItems,
            HoldResourceManager.LockedResources lockedResources
    ) {
        if (lockedResources.seatById().size() != requestedItems.seatIds().size()) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid seat ids");
        }
        if (lockedResources.sectionById().size() != requestedItems.sectionIds().size()) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid section ids");
        }
    }

    private void applyHoldItems(
            Hold hold,
            List<RequestedItem> requestedItems,
            HoldResourceManager.LockedResources lockedResources
    ) {
        for (RequestedItem requestedItem : requestedItems) {
            if (requestedItem.isSeat()) {
                ShowSeat showSeat = lockedResources.seatById().get(requestedItem.seatId());
                try {
                    showSeat.hold();
                } catch (IllegalStateException exception) {
                    throw new ResponseStatusException(CONFLICT, exception.getMessage());
                }
                hold.addSeatItem(requestedItem.seatId(), showSeat.getPrice());
                continue;
            }

            ShowSectionInventory inventory = lockedResources.sectionById().get(requestedItem.sectionId());
            try {
                inventory.hold(requestedItem.qty());
            } catch (IllegalStateException exception) {
                throw new ResponseStatusException(CONFLICT, exception.getMessage());
            }
            hold.addSectionItem(requestedItem.sectionId(), requestedItem.qty(), inventory.getPrice());
        }
    }

    private record RequestedItems(
            List<RequestedItem> items,
            Set<Long> seatIds,
            Set<Long> sectionIds
    ) {
    }

    private record RequestedItem(
            Long seatId,
            Long sectionId,
            int qty
    ) {
        static RequestedItem seat(Long seatId) {
            return new RequestedItem(seatId, null, 1);
        }

        static RequestedItem section(Long sectionId, int qty) {
            return new RequestedItem(null, sectionId, qty);
        }

        boolean isSeat() {
            return seatId != null;
        }
    }
}