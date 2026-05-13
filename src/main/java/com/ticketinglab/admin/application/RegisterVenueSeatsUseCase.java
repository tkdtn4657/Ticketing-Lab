package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.CreatedCountResponse;
import com.ticketinglab.admin.presentation.dto.RegisterVenueSeatsRequest;
import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
import com.ticketinglab.venue.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RegisterVenueSeatsUseCase {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final SectionRepository sectionRepository;

    @Transactional
    public CreatedCountResponse execute(Long venueId, RegisterVenueSeatsRequest request) {
        venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "venue not found"));

        List<String> labels = request.seats().stream()
                .map(RegisterVenueSeatsRequest.SeatItem::label)
                .toList();
        validateDistinctLabels(labels);

        if (!seatRepository.findAllByVenueIdAndLabelIn(venueId, labels).isEmpty()) {
            throw new ResponseStatusException(CONFLICT, "seat already exists");
        }

        Map<Long, Section> sectionById = loadSectionById(venueId, request);
        List<Seat> seats = request.seats().stream()
                .map(item -> Seat.create(
                        item.label(),
                        item.rowNo(),
                        item.colNo(),
                        venueId,
                        item.sectionId() == null ? null : sectionById.get(item.sectionId())
                ))
                .toList();

        seatRepository.saveAll(seats);
        return new CreatedCountResponse(seats.size());
    }

    private void validateDistinctLabels(List<String> labels) {
        if (new HashSet<>(labels).size() != labels.size()) {
            throw new ResponseStatusException(CONFLICT, "duplicate seat labels");
        }
    }

    private Map<Long, Section> loadSectionById(Long venueId, RegisterVenueSeatsRequest request) {
        List<Long> sectionIds = request.seats().stream()
                .map(RegisterVenueSeatsRequest.SeatItem::sectionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();

        if (sectionIds.isEmpty()) {
            return Map.of();
        }

        List<Section> sections = sectionRepository.findAllByVenueIdAndIdIn(venueId, sectionIds);
        if (sections.size() != sectionIds.size()) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid section ids");
        }

        boolean hasGeneralAdmissionSection = sections.stream().anyMatch(Section::isGeneralAdmissionType);
        if (hasGeneralAdmissionSection) {
            throw new ResponseStatusException(BAD_REQUEST, "seats can be assigned only to assigned-seat sections");
        }

        return sections.stream().collect(Collectors.toMap(Section::getId, Function.identity()));
    }
}
