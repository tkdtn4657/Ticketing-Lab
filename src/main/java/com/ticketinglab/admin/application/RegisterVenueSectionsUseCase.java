package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.CreatedCountResponse;
import com.ticketinglab.admin.presentation.dto.RegisterVenueSectionsRequest;
import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
import com.ticketinglab.venue.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RegisterVenueSectionsUseCase {

    private final VenueRepository venueRepository;
    private final SectionRepository sectionRepository;

    @Transactional
    public CreatedCountResponse execute(Long venueId, RegisterVenueSectionsRequest request) {
        venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "venue not found"));

        List<String> names = request.sections().stream()
                .map(RegisterVenueSectionsRequest.SectionItem::name)
                .toList();
        validateDistinctNames(names);

        if (!sectionRepository.findAllByVenueIdAndNameIn(venueId, names).isEmpty()) {
            throw new ResponseStatusException(CONFLICT, "section already exists");
        }

        List<Section> sections = request.sections().stream()
                .map(item -> Section.create(item.name(), venueId))
                .toList();

        sectionRepository.saveAll(sections);
        return new CreatedCountResponse(sections.size());
    }

    private void validateDistinctNames(List<String> names) {
        if (new HashSet<>(names).size() != names.size()) {
            throw new ResponseStatusException(CONFLICT, "duplicate section names");
        }
    }
}