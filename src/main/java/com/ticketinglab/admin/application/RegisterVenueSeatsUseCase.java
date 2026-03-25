package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.CreatedCountResponse;
import com.ticketinglab.admin.presentation.dto.RegisterVenueSeatsRequest;
import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
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
public class RegisterVenueSeatsUseCase {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

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

        List<Seat> seats = request.seats().stream()
                .map(item -> Seat.create(item.label(), item.rowNo(), item.colNo(), venueId))
                .toList();

        seatRepository.saveAll(seats);
        return new CreatedCountResponse(seats.size());
    }

    private void validateDistinctLabels(List<String> labels) {
        if (new HashSet<>(labels).size() != labels.size()) {
            throw new ResponseStatusException(CONFLICT, "duplicate seat labels");
        }
    }
}