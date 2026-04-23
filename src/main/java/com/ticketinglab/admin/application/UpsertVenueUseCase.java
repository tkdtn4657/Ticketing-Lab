package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.VenueUpsertRequest;
import com.ticketinglab.admin.presentation.dto.VenueUpsertResponse;
import com.ticketinglab.venue.domain.Venue;
import com.ticketinglab.venue.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class UpsertVenueUseCase {

    private final VenueRepository venueRepository;

    @Transactional
    public VenueUpsertResponse execute(Long userId, VenueUpsertRequest request) {
        Venue venue = venueRepository.findByCode(request.code())
                .map(existingVenue -> {
                    if (!existingVenue.isCreatorMissing() && !existingVenue.isCreatedBy(userId)) {
                        throw new ResponseStatusException(FORBIDDEN, "venue owner mismatch");
                    }
                    existingVenue.updateInfo(request.name(), request.address());
                    existingVenue.assignCreatorIfMissing(userId);
                    return existingVenue;
                })
                .orElseGet(() -> Venue.create(request.code(), request.name(), request.address(), userId));

        Venue savedVenue = venueRepository.save(venue);
        return new VenueUpsertResponse(savedVenue.getId());
    }
}
