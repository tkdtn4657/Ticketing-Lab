package com.ticketinglab.admin.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketinglab.admin.presentation.dto.CreateEventRequest;
import com.ticketinglab.admin.presentation.dto.CreateShowRequest;
import com.ticketinglab.admin.presentation.dto.CreateShowSectionInventoriesRequest;
import com.ticketinglab.admin.presentation.dto.CreateShowSeatsRequest;
import com.ticketinglab.admin.presentation.dto.RegisterVenueSectionsRequest;
import com.ticketinglab.admin.presentation.dto.RegisterVenueSeatsRequest;
import com.ticketinglab.admin.presentation.dto.VenueUpsertRequest;
import com.ticketinglab.auth.infrastructure.jwt.JwtTokenProvider;
import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
import com.ticketinglab.venue.domain.Venue;
import com.ticketinglab.venue.domain.VenueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private ShowSectionInventoryRepository showSectionInventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("ADM-001 POST /api/admin/venues/upsert creates or updates a venue")
    void adm001_upsertVenue_createsOrUpdatesVenue() throws Exception {
        String adminToken = createAccessToken("ADMIN");

        MvcResult createResult = mockMvc.perform(post("/api/admin/venues/upsert")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new VenueUpsertRequest("SEOUL-HALL", "Seoul Hall", "Gangnam"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venueId").isNumber())
                .andReturn();

        Long venueId = body(createResult).get("venueId").asLong();

        mockMvc.perform(post("/api/admin/venues/upsert")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new VenueUpsertRequest("SEOUL-HALL", "Seoul Arena", "Jamsil"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venueId").value(venueId));

        Venue venue = venueRepository.findById(venueId).orElseThrow();
        assertThat(venue.getName()).isEqualTo("Seoul Arena");
        assertThat(venue.getAddress()).isEqualTo("Jamsil");
    }

    @Test
    @DisplayName("ADM-001 rejects venue upsert by another admin")
    void adm001_upsertVenue_rejectsCrossOwnerUpdate() throws Exception {
        String ownerToken = createAccessToken("ADMIN");
        String otherAdminToken = createAccessToken("ADMIN");

        Long venueId = createVenue(ownerToken, "OWNER-ONLY-HALL", "Owner Hall");

        mockMvc.perform(post("/api/admin/venues/upsert")
                        .header("Authorization", bearer(otherAdminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new VenueUpsertRequest("OWNER-ONLY-HALL", "Hijacked Hall", "Nowhere"))))
                .andExpect(status().isForbidden());

        Venue venue = venueRepository.findById(venueId).orElseThrow();
        assertThat(venue.getName()).isEqualTo("Owner Hall");
        assertThat(venue.getAddress()).isEqualTo("Seoul");
    }

    @Test
    @DisplayName("ADM-002 ADM-003 and reference lookup APIs register and list venue data")
    void adm002_003_registerAndListVenueReferenceData() throws Exception {
        String adminToken = createAccessToken("ADMIN");
        Venue venue = venueRepository.save(Venue.create("BUSAN-HALL", "Busan Hall", "Centum"));

        mockMvc.perform(post("/api/admin/venues/{venueId}/seats", venue.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new RegisterVenueSeatsRequest(
                                List.of(
                                        new RegisterVenueSeatsRequest.SeatItem("A1", 1, 1),
                                        new RegisterVenueSeatsRequest.SeatItem("A2", 1, 2)
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCount").value(2));

        mockMvc.perform(post("/api/admin/venues/{venueId}/sections", venue.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new RegisterVenueSectionsRequest(
                                List.of(
                                        new RegisterVenueSectionsRequest.SectionItem("R"),
                                        new RegisterVenueSectionsRequest.SectionItem("S")
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCount").value(2));

        mockMvc.perform(get("/api/admin/venues/{venueId}/seats", venue.getId())
                        .header("Authorization", bearer(adminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seats.length()").value(2))
                .andExpect(jsonPath("$.seats[0].label").value("A1"))
                .andExpect(jsonPath("$.seats[0].seatId").isNumber())
                .andExpect(jsonPath("$.seats[1].label").value("A2"));

        mockMvc.perform(get("/api/admin/venues/{venueId}/sections", venue.getId())
                        .header("Authorization", bearer(adminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(2))
                .andExpect(jsonPath("$.sections[0].name").value("R"))
                .andExpect(jsonPath("$.sections[0].sectionId").isNumber())
                .andExpect(jsonPath("$.sections[1].name").value("S"));

        assertThat(seatRepository.findAllByVenueIdAndLabelIn(venue.getId(), List.of("A1", "A2"))).hasSize(2);
        assertThat(sectionRepository.findAllByVenueIdAndNameIn(venue.getId(), List.of("R", "S"))).hasSize(2);
    }

    @Test
    @DisplayName("ADM-004 and ADM-005 create event and show")
    void adm004_005_createEventAndShow() throws Exception {
        String adminToken = createAccessToken("ADMIN");
        Long adminUserId = jwtTokenProvider.getUserId(adminToken);
        Long venueId = createVenue(adminToken, "DAEGU-HALL", "Daegu Hall");

        MvcResult eventResult = mockMvc.perform(post("/api/admin/events")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateEventRequest("Jazz Night", "Late night live", "PUBLISHED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").isNumber())
                .andReturn();

        Long eventId = body(eventResult).get("eventId").asLong();

        MvcResult showResult = mockMvc.perform(post("/api/admin/shows")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateShowRequest(
                                eventId,
                                venueId,
                                LocalDateTime.of(2026, 4, 20, 19, 30)
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.showId").isNumber())
                .andReturn();

        Long showId = body(showResult).get("showId").asLong();

        Event savedEvent = eventRepository.findById(eventId).orElseThrow();
        assertThat(savedEvent.getCreatedByUserId()).isEqualTo(adminUserId);
        assertThat(showRepository.findAllByEventId(eventId)).hasSize(1);
        assertThat(showRepository.findAllByEventId(eventId).get(0).isHeldAt(venueId)).isTrue();
        assertThat(showRepository.findById(showId).orElseThrow().getCreatedByUserId()).isEqualTo(adminUserId);
    }

    @Test
    @DisplayName("ADM-005 rejects show creation with resources owned by another admin")
    void adm005_createShow_rejectsCrossOwnerResources() throws Exception {
        String ownerToken = createAccessToken("ADMIN");
        String otherAdminToken = createAccessToken("ADMIN");
        Long ownerVenueId = createVenue(ownerToken, "SHOW-OWNER-HALL", "Show Owner Hall");
        Long otherVenueId = createVenue(otherAdminToken, "SHOW-OTHER-HALL", "Show Other Hall");
        Long ownerEventId = createEvent(ownerToken, "Show Owner Event");
        Long otherEventId = createEvent(otherAdminToken, "Show Other Event");

        mockMvc.perform(post("/api/admin/shows")
                        .header("Authorization", bearer(otherAdminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateShowRequest(
                                ownerEventId,
                                otherVenueId,
                                LocalDateTime.of(2026, 4, 21, 19, 30)
                        ))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/shows")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateShowRequest(
                                ownerEventId,
                                otherVenueId,
                                LocalDateTime.of(2026, 4, 22, 19, 30)
                        ))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/shows")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateShowRequest(
                                otherEventId,
                                ownerVenueId,
                                LocalDateTime.of(2026, 4, 23, 19, 30)
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADM-010 ADM-011 ADM-012 and master APIs separate owner-only and all-resource lookup")
    void adm010_011_012_listAdminResources_separatesOwnerOnlyAndMasterLookup() throws Exception {
        String adminToken = createAccessToken("ADMIN");
        String otherAdminToken = createAccessToken("ADMIN");
        String masterAdminToken = createAccessToken("MASTER_ADMIN");
        Long adminUserId = jwtTokenProvider.getUserId(adminToken);
        Long otherAdminUserId = jwtTokenProvider.getUserId(otherAdminToken);

        Long venueId = createVenue(adminToken, "OWNER-HALL", "Owner Hall");
        Long otherVenueId = createVenue(otherAdminToken, "OTHER-HALL", "Other Hall");
        Long eventId = createEvent(adminToken, "Owner Event");
        Long otherEventId = createEvent(otherAdminToken, "Other Event");
        Long showId = createShow(adminToken, eventId, venueId);
        Long otherShowId = createShow(otherAdminToken, otherEventId, otherVenueId);

        MvcResult myVenuesResult = mockMvc.perform(get("/api/admin/venues")
                        .header("Authorization", bearer(adminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venues.length()").value(1))
                .andExpect(jsonPath("$.venues[0].venueId").value(venueId))
                .andExpect(jsonPath("$.venues[0].createdByUserId").value(adminUserId))
                .andReturn();

        MvcResult myEventsResult = mockMvc.perform(get("/api/admin/events")
                        .header("Authorization", bearer(adminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(1))
                .andExpect(jsonPath("$.events[0].eventId").value(eventId))
                .andExpect(jsonPath("$.events[0].createdByUserId").value(adminUserId))
                .andReturn();

        MvcResult myShowsResult = mockMvc.perform(get("/api/admin/shows")
                        .header("Authorization", bearer(adminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shows.length()").value(1))
                .andExpect(jsonPath("$.shows[0].showId").value(showId))
                .andExpect(jsonPath("$.shows[0].eventId").value(eventId))
                .andExpect(jsonPath("$.shows[0].eventTitle").value("Owner Event"))
                .andExpect(jsonPath("$.shows[0].createdByUserId").value(adminUserId))
                .andReturn();

        MvcResult masterVenuesResult = mockMvc.perform(get("/api/master/venues")
                        .header("Authorization", bearer(masterAdminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venues.length()").value(2))
                .andReturn();

        MvcResult masterEventsResult = mockMvc.perform(get("/api/master/events")
                        .header("Authorization", bearer(masterAdminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(2))
                .andReturn();

        MvcResult masterShowsResult = mockMvc.perform(get("/api/master/shows")
                        .header("Authorization", bearer(masterAdminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shows.length()").value(2))
                .andReturn();

        mockMvc.perform(get("/api/master/venues")
                        .header("Authorization", bearer(adminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/master/events")
                        .header("Authorization", bearer(adminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/master/shows")
                        .header("Authorization", bearer(adminToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        assertThat(venueRepository.findById(venueId).orElseThrow().getCreatedByUserId()).isEqualTo(adminUserId);
        assertThat(venueRepository.findById(otherVenueId).orElseThrow().getCreatedByUserId()).isEqualTo(otherAdminUserId);
        assertThat(eventRepository.findById(otherEventId).orElseThrow().getCreatedByUserId()).isEqualTo(otherAdminUserId);
        assertThat(showRepository.findById(otherShowId).orElseThrow().getCreatedByUserId()).isEqualTo(otherAdminUserId);

        JsonNode myVenues = body(myVenuesResult).get("venues");
        JsonNode myEvents = body(myEventsResult).get("events");
        JsonNode myShows = body(myShowsResult).get("shows");
        JsonNode masterVenues = body(masterVenuesResult).get("venues");
        JsonNode masterEvents = body(masterEventsResult).get("events");
        JsonNode masterShows = body(masterShowsResult).get("shows");
        assertThat(myVenues.findValuesAsText("venueId")).doesNotContain(String.valueOf(otherVenueId));
        assertThat(myEvents.findValuesAsText("eventId")).doesNotContain(String.valueOf(otherEventId));
        assertThat(myShows.findValuesAsText("showId")).doesNotContain(String.valueOf(otherShowId));
        assertThat(masterVenues.findValuesAsText("venueId")).contains(String.valueOf(venueId), String.valueOf(otherVenueId));
        assertThat(masterEvents.findValuesAsText("eventId")).contains(String.valueOf(eventId), String.valueOf(otherEventId));
        assertThat(masterShows.findValuesAsText("showId")).contains(String.valueOf(showId), String.valueOf(otherShowId));
    }

    @Test
    @DisplayName("ADM-006 and ADM-007 create show inventory reflected in availability")
    void adm006_007_createShowInventory_reflectedInAvailability() throws Exception {
        String adminToken = createAccessToken("ADMIN");
        Venue venue = venueRepository.save(Venue.create("INCHEON-HALL", "Incheon Hall", "Songdo"));
        Event event = eventRepository.save(Event.create("Rock Festa", "Inventory test", EventStatus.PUBLISHED));
        Show show = showRepository.save(Show.schedule(event, LocalDateTime.of(2026, 5, 1, 19, 0), venue.getId()));
        Seat seat = seatRepository.save(Seat.create("B1", 2, 1, venue.getId()));
        Section section = sectionRepository.save(Section.create("VIP", venue.getId()));

        mockMvc.perform(post("/api/admin/shows/{showId}/show-seats", show.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateShowSeatsRequest(
                                List.of(new CreateShowSeatsRequest.Item(seat.getId(), 150000))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCount").value(1));

        mockMvc.perform(post("/api/admin/shows/{showId}/section-inventories", show.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateShowSectionInventoriesRequest(
                                List.of(new CreateShowSectionInventoriesRequest.Item(section.getId(), 120000, 100))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCount").value(1));

        assertThat(showSeatRepository.findAllByShowId(show.getId())).hasSize(1);
        assertThat(showSectionInventoryRepository.findAllByShowId(show.getId())).hasSize(1);

        mockMvc.perform(get("/api/shows/{showId}/availability", show.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seats.length()").value(1))
                .andExpect(jsonPath("$.seats[0].seatId").value(seat.getId()))
                .andExpect(jsonPath("$.seats[0].price").value(150000))
                .andExpect(jsonPath("$.seats[0].available").value(true))
                .andExpect(jsonPath("$.sections.length()").value(1))
                .andExpect(jsonPath("$.sections[0].sectionId").value(section.getId()))
                .andExpect(jsonPath("$.sections[0].price").value(120000))
                .andExpect(jsonPath("$.sections[0].remainingQty").value(100));
    }

    @Test
    @DisplayName("ADMIN API rejects non-admin users")
    void adminApi_nonAdmin_returnsForbidden() throws Exception {
        String userToken = createAccessToken("USER");

        mockMvc.perform(post("/api/admin/events")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateEventRequest("Forbidden Event", "User token", "DRAFT"))))
                .andExpect(status().isForbidden());
    }

    private String createAccessToken(String role) {
        String email = role.toLowerCase() + System.nanoTime() + "@example.com";
        String passwordHash = passwordEncoder.encode("password123");
        User user = switch (role) {
            case "ADMIN" -> userRepository.save(User.createAdmin(email, passwordHash));
            case "MASTER_ADMIN" -> userRepository.save(User.createMasterAdmin(email, passwordHash));
            default -> userRepository.save(User.createUser(email, passwordHash));
        };

        return jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
    }

    private Long createVenue(String adminToken, String code, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/venues/upsert")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new VenueUpsertRequest(code, name, "Seoul"))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("venueId").asLong();
    }

    private Long createEvent(String adminToken, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/events")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateEventRequest(title, "Admin list test", "PUBLISHED"))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("eventId").asLong();
    }

    private Long createShow(String adminToken, Long eventId, Long venueId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/shows")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateShowRequest(
                                eventId,
                                venueId,
                                LocalDateTime.of(2026, 9, 1, 19, 0)
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("showId").asLong();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
