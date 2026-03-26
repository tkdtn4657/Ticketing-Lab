package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.CreateShowSectionInventoriesRequest;
import com.ticketinglab.admin.presentation.dto.CreatedCountResponse;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CreateShowSectionInventoriesUseCase {

    private final ShowRepository showRepository;
    private final SectionRepository sectionRepository;
    private final ShowSectionInventoryRepository showSectionInventoryRepository;

    @Transactional
    public CreatedCountResponse execute(Long showId, CreateShowSectionInventoriesRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "show not found"));

        List<Long> sectionIds = request.items().stream()
                .map(CreateShowSectionInventoriesRequest.Item::sectionId)
                .toList();
        validateDistinctSectionIds(sectionIds);

        List<Section> sections = sectionRepository.findAllByVenueIdAndIdIn(show.getVenueId(), sectionIds);
        if (sections.size() != sectionIds.size()) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid section ids");
        }

        if (!showSectionInventoryRepository.findAllByShowIdAndSectionIdIn(showId, sectionIds).isEmpty()) {
            throw new ResponseStatusException(CONFLICT, "show section inventory already exists");
        }

        Map<Long, Section> sectionById = sections.stream()
                .collect(Collectors.toMap(Section::getId, Function.identity()));

        List<ShowSectionInventory> inventories = request.items().stream()
                .map(item -> ShowSectionInventory.open(
                        show,
                        sectionById.get(item.sectionId()),
                        item.price(),
                        item.capacity()
                ))
                .toList();

        showSectionInventoryRepository.saveAll(inventories);
        return new CreatedCountResponse(inventories.size());
    }

    private void validateDistinctSectionIds(List<Long> sectionIds) {
        if (new HashSet<>(sectionIds).size() != sectionIds.size()) {
            throw new ResponseStatusException(CONFLICT, "duplicate section ids");
        }
    }
}