package com.ticketinglab.show.application;

import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.show.presentation.dto.SectionAvailabilityResponse;
import com.ticketinglab.show.presentation.dto.ShowAvailabilityResponse;
import com.ticketinglab.show.presentation.dto.ShowSeatAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class GetShowAvailabilityUseCase {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowSectionInventoryRepository showSectionInventoryRepository;

    @Transactional(readOnly = true)
    public ShowAvailabilityResponse execute(Long showId) {
        showRepository.findById(showId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "show not found"));

        List<ShowSeatAvailabilityResponse> seats = showSeatRepository.findAllByShowId(showId).stream()
                .sorted(seatAvailabilityOrder())
                .map(ShowSeatAvailabilityResponse::from)
                .toList();

        List<SectionAvailabilityResponse> sections = showSectionInventoryRepository.findAllByShowId(showId).stream()
                .sorted(sectionAvailabilityOrder())
                .map(SectionAvailabilityResponse::from)
                .toList();

        return new ShowAvailabilityResponse(seats, sections);
    }

    private Comparator<ShowSeat> seatAvailabilityOrder() {
        return Comparator
                .comparing((ShowSeat showSeat) -> showSeat.getSeat().getRowNo(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(showSeat -> showSeat.getSeat().getColNo(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(showSeat -> showSeat.getSeat().getId());
    }

    private Comparator<ShowSectionInventory> sectionAvailabilityOrder() {
        return Comparator
                .comparing((ShowSectionInventory inventory) -> inventory.getSection().getName())
                .thenComparing(inventory -> inventory.getSection().getId());
    }
}
