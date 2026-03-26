package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.CreateEventRequest;
import com.ticketinglab.admin.presentation.dto.CreateEventResponse;
import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class CreateEventUseCase {

    private final EventRepository eventRepository;

    @Transactional
    public CreateEventResponse execute(CreateEventRequest request) {
        EventStatus status = resolveStatus(request.status());
        Event event = eventRepository.save(Event.create(request.title(), request.desc(), status));
        return new CreateEventResponse(event.getId());
    }

    private EventStatus resolveStatus(String rawStatus) {
        try {
            return EventStatus.from(rawStatus);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid event status");
        }
    }
}