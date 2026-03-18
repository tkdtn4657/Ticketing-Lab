package com.ticketinglab.show.presentation;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.show.domain.ShowSeatStatus;
import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
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
class ShowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private ShowSectionInventoryRepository showSectionInventoryRepository;

    @Test
    @DisplayName("SHW-001 GET /api/shows/{showId}/availability returns seats and sections")
    void shw001_availability_returnsSeatsAndSections() throws Exception {
        Event event = eventRepository.save(
                Event.create("Grand Concert", "Availability test show", EventStatus.PUBLISHED)
        );
        Show show = showRepository.save(
                Show.schedule(event, LocalDateTime.of(2026, 4, 5, 19, 0), 301L)
        );

        Seat secondSeat = seatRepository.save(Seat.create("A2", 1, 2, 301L));
        Seat firstSeat = seatRepository.save(Seat.create("A1", 1, 1, 301L));
        Section section = sectionRepository.save(Section.create("R", 301L));

        showSeatRepository.save(ShowSeat.create(show, secondSeat, 150000, ShowSeatStatus.SOLD));
        showSeatRepository.save(ShowSeat.create(show, firstSeat, 150000, ShowSeatStatus.AVAILABLE));
        showSectionInventoryRepository.save(
                ShowSectionInventory.create(show, section, 120000, 100, 20, 5)
        );

        mockMvc.perform(get("/api/shows/{showId}/availability", show.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seats.length()").value(2))
                .andExpect(jsonPath("$.seats[0].seatId").value(firstSeat.getId()))
                .andExpect(jsonPath("$.seats[0].label").value("A1"))
                .andExpect(jsonPath("$.seats[0].rowNo").value(1))
                .andExpect(jsonPath("$.seats[0].colNo").value(1))
                .andExpect(jsonPath("$.seats[0].price").value(150000))
                .andExpect(jsonPath("$.seats[0].available").value(true))
                .andExpect(jsonPath("$.seats[1].seatId").value(secondSeat.getId()))
                .andExpect(jsonPath("$.seats[1].available").value(false))
                .andExpect(jsonPath("$.sections.length()").value(1))
                .andExpect(jsonPath("$.sections[0].sectionId").value(section.getId()))
                .andExpect(jsonPath("$.sections[0].name").value("R"))
                .andExpect(jsonPath("$.sections[0].price").value(120000))
                .andExpect(jsonPath("$.sections[0].remainingQty").value(75));
    }

    @Test
    @DisplayName("SHW-001 show not found returns 404")
    void shw001_availability_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/shows/{showId}/availability", 99999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
