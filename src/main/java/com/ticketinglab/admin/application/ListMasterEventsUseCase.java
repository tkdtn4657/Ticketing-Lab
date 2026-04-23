package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.AdminEventListResponse;
import com.ticketinglab.admin.presentation.dto.AdminEventResponse;
import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMasterEventsUseCase {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public AdminEventListResponse execute() {
        List<Event> events = eventRepository.findAll();

        return new AdminEventListResponse(
                events.stream()
                        .map(AdminEventResponse::from)
                        .toList()
        );
    }
}
