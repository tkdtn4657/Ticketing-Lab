package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.VenueSectionListResponse;
import com.ticketinglab.admin.presentation.dto.VenueSectionResponse;
import com.ticketinglab.venue.domain.SectionRepository;
import com.ticketinglab.venue.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ListVenueSectionsUseCase {

    private final VenueRepository venueRepository;
    private final SectionRepository sectionRepository;

    @Transactional(readOnly = true)
    public VenueSectionListResponse execute(Long venueId) {
        venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "venue not found"));

        List<VenueSectionResponse> sections = sectionRepository.findAllByVenueId(venueId).stream()
                .map(VenueSectionResponse::from)
                .toList();

        return new VenueSectionListResponse(sections);
    }
}