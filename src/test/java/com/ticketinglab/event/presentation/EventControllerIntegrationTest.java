package com.ticketinglab.event.presentation;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.event.domain.ShowStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ShowRepository showRepository;

    @Test
    @DisplayName("EVT-001 GET /api/events returns events filtered by status")
    void evt001_list_returnsEventsFilteredByStatus() throws Exception {
        Event publishedEvent = eventRepository.save(
                Event.create("Spring Festival", "Outdoors concert event", EventStatus.PUBLISHED)
        );
        eventRepository.save(
                Event.create("Draft Internal Event", "Not opened yet", EventStatus.DRAFT)
        );

        mockMvc.perform(get("/api/events")
                        .queryParam("status", "PUBLISHED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(1))
                .andExpect(jsonPath("$.events[0].eventId").value(publishedEvent.getId()))
                .andExpect(jsonPath("$.events[0].title").value("Spring Festival"))
                .andExpect(jsonPath("$.events[0].description").value("Outdoors concert event"))
                .andExpect(jsonPath("$.events[0].status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("EVT-001 invalid status filter returns 400")
    void evt001_list_invalidStatus_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/events")
                        .queryParam("status", "UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("EVT-002 GET /api/events/{eventId} returns event with shows")
    void evt002_detail_returnsEventAndShows() throws Exception {
        Event event = eventRepository.save(
                Event.create("Indie Night", "Live house performance", EventStatus.PUBLISHED)
        );
        Show firstShow = showRepository.save(
                Show.schedule(event, LocalDateTime.of(2026, 4, 1, 19, 0), 101L)
        );
        Show secondShow = showRepository.save(
                Show.create(event, LocalDateTime.of(2026, 4, 2, 19, 0), ShowStatus.SOLD_OUT, 101L)
        );

        mockMvc.perform(get("/api/events/{eventId}", event.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event.eventId").value(event.getId()))
                .andExpect(jsonPath("$.event.title").value("Indie Night"))
                .andExpect(jsonPath("$.event.description").value("Live house performance"))
                .andExpect(jsonPath("$.event.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.shows.length()").value(2))
                .andExpect(jsonPath("$.shows[0].showId").value(firstShow.getId()))
                .andExpect(jsonPath("$.shows[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$.shows[0].venueId").value(101))
                .andExpect(jsonPath("$.shows[1].showId").value(secondShow.getId()))
                .andExpect(jsonPath("$.shows[1].status").value("SOLD_OUT"));
    }

    @Test
    @DisplayName("EVT-002 event not found returns 404")
    void evt002_detail_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/events/{eventId}", 99999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
