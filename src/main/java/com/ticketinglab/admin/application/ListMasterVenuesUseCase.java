package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.AdminVenueListResponse;
import com.ticketinglab.admin.presentation.dto.AdminVenueResponse;
import com.ticketinglab.venue.domain.Venue;
import com.ticketinglab.venue.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMasterVenuesUseCase {

    private final VenueRepository venueRepository;

    @Transactional(readOnly = true)
    public AdminVenueListResponse execute() {
        List<Venue> venues = venueRepository.findAll();

        return new AdminVenueListResponse(
                venues.stream()
                        .map(AdminVenueResponse::from)
                        .toList()
        );
    }
}
