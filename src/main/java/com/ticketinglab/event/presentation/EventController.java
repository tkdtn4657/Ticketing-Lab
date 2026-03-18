package com.ticketinglab.event.presentation;

import com.ticketinglab.event.application.GetEventDetailUseCase;
import com.ticketinglab.event.application.ListEventsUseCase;
import com.ticketinglab.event.presentation.dto.EventDetailResponse;
import com.ticketinglab.event.presentation.dto.EventListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final ListEventsUseCase listEventsUseCase;
    private final GetEventDetailUseCase getEventDetailUseCase;

    @GetMapping
    public ResponseEntity<EventListResponse> list(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(listEventsUseCase.execute(status));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponse> detail(@PathVariable Long eventId) {
        return ResponseEntity.ok(getEventDetailUseCase.execute(eventId));
    }
}
