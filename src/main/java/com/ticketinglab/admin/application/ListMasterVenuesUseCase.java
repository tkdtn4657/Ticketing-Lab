package com.ticketinglab.admin.application;

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
    public List<Venue> execute() {
        return venueRepository.findAll();
    }
}
