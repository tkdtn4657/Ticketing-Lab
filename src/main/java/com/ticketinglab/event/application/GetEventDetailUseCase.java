package com.ticketinglab.event.application;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.event.presentation.dto.EventDetailInfoResponse;
import com.ticketinglab.event.presentation.dto.EventDetailResponse;
import com.ticketinglab.event.presentation.dto.ShowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class GetEventDetailUseCase {

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;

    @Transactional(readOnly = true)
    public EventDetailResponse execute(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "event not found"));

        List<ShowResponse> shows = showRepository.findAllByEventId(eventId).stream()
                .map(ShowResponse::from)
                .toList();

        return new EventDetailResponse(EventDetailInfoResponse.from(event), shows);
    }
}
