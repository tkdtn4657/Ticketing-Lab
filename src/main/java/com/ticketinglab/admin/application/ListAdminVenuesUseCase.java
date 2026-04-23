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
public class ListAdminVenuesUseCase {

    private final VenueRepository venueRepository;

    @Transactional(readOnly = true)
    public AdminVenueListResponse execute(Long userId) {
        List<Venue> venues = venueRepository.findAllByCreatedByUserId(userId);

        return new AdminVenueListResponse(
                venues.stream()
                        .map(AdminVenueResponse::from)
                        .toList()
        );
    }
}
