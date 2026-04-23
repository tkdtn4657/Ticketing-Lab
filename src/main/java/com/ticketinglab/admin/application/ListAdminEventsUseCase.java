package com.ticketinglab.admin.application;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAdminEventsUseCase {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<Event> execute(Long userId) {
        return eventRepository.findAllByCreatedByUserId(userId);
    }
}
