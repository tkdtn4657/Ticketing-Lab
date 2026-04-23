package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.CreateShowRequest;
import com.ticketinglab.admin.presentation.dto.CreateShowResponse;
import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.venue.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CreateShowUseCase {

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public CreateShowResponse execute(Long userId, CreateShowRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "event not found"));

        venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "venue not found"));

        Show show = showRepository.save(Show.schedule(event, request.startAt(), request.venueId(), userId));
        return new CreateShowResponse(show.getId());
    }
}
