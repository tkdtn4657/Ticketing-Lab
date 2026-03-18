package com.ticketinglab.event.application;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.presentation.dto.EventListResponse;
import com.ticketinglab.event.presentation.dto.EventSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class ListEventsUseCase {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public EventListResponse execute(String rawStatus) {
        EventStatus status = resolveStatus(rawStatus);
        List<Event> events = status == null
                ? eventRepository.findAll()
                : eventRepository.findAllByStatus(status);

        List<EventSummaryResponse> responses = events.stream()
                .map(EventSummaryResponse::from)
                .toList();

        return new EventListResponse(responses);
    }

    private EventStatus resolveStatus(String rawStatus) {
        if (!StringUtils.hasText(rawStatus)) {
            return null;
        }

        try {
            return EventStatus.from(rawStatus);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid event status");
        }
    }
}
