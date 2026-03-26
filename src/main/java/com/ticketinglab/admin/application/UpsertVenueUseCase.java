package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.VenueUpsertRequest;
import com.ticketinglab.admin.presentation.dto.VenueUpsertResponse;
import com.ticketinglab.venue.domain.Venue;
import com.ticketinglab.venue.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpsertVenueUseCase {

    private final VenueRepository venueRepository;

    @Transactional
    public VenueUpsertResponse execute(VenueUpsertRequest request) {
        Venue venue = venueRepository.findByCode(request.code())
                .map(existingVenue -> {
                    existingVenue.updateInfo(request.name(), request.address());
                    return existingVenue;
                })
                .orElseGet(() -> Venue.create(request.code(), request.name(), request.address()));

        Venue savedVenue = venueRepository.save(venue);
        return new VenueUpsertResponse(savedVenue.getId());
    }
}