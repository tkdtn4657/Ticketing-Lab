package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.VenueSeatListResponse;
import com.ticketinglab.admin.presentation.dto.VenueSeatResponse;
import com.ticketinglab.venue.domain.SeatRepository;
import com.ticketinglab.venue.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ListVenueSeatsUseCase {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public VenueSeatListResponse execute(Long venueId) {
        venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "venue not found"));

        List<VenueSeatResponse> seats = seatRepository.findAllByVenueId(venueId).stream()
                .map(VenueSeatResponse::from)
                .toList();

        return new VenueSeatListResponse(seats);
    }
}