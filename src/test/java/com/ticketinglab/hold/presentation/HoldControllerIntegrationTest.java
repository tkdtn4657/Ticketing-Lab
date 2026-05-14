package com.ticketinglab.hold.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketinglab.auth.domain.TokenSession;
import com.ticketinglab.auth.domain.TokenSessionRepository;
import com.ticketinglab.auth.infrastructure.jwt.JwtTokenProvider;
import com.ticketinglab.auth.presentation.dto.TokenPair;
import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.hold.domain.HoldStatus;
import com.ticketinglab.hold.presentation.dto.CreateHoldRequest;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.show.domain.ShowSeatStatus;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
import org.junit.jupiter.api.AfterEach;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class HoldControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private HoldRepository holdRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenSessionRepository tokenSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<Long> createdUserIds = new ArrayList<>();

    @AfterEach
    void clearTokenSessions() {
        createdUserIds.forEach(tokenSessionRepository::deleteByUserId);
        createdUserIds.clear();
    }

    @Test
    @DisplayName("HLD-001 POST /api/holds creates a hold and updates inventory")
    void hld001_createHold_updatesInventory() throws Exception {
        UserSession session = createUserSession();
        HoldFixture fixture = createFixture();

        MvcResult result = mockMvc.perform(post("/api/holds")
                        .header("Authorization", bearer(session.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateHoldRequest(
                                fixture.show().getId(),
                                List.of(
                                        new CreateHoldRequest.Item(fixture.firstSeat().getId()),
                                        new CreateHoldRequest.Item(fixture.secondSeat().getId())
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").isString())
                .andExpect(jsonPath("$.expiresAt").isString())
                .andReturn();

        String holdId = body(result).get("holdId").asText();
        Hold hold = holdRepository.findById(holdId).orElseThrow();

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.ACTIVE);
        assertThat(hold.getItems()).hasSize(2);
        assertThat(showSeatRepository.findAllByShowId(fixture.show().getId()))
                .extracting(ShowSeat::getStatus)
                .containsOnly(ShowSeatStatus.HELD);
    }

    @Test
    @DisplayName("HLD-002 GET /api/holds/{holdId} returns hold and items")
    void hld002_getHold_returnsHoldAndItems() throws Exception {
        UserSession session = createUserSession();
        HoldFixture fixture = createFixture();
        String holdId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId(), fixture.secondSeat().getId())
        );

        mockMvc.perform(get("/api/holds/{holdId}", holdId)
                        .header("Authorization", bearer(session.accessToken()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hold.holdId").value(holdId))
                .andExpect(jsonPath("$.hold.showId").value(fixture.show().getId()))
                .andExpect(jsonPath("$.hold.status").value("ACTIVE"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].type").value("SEAT"))
                .andExpect(jsonPath("$.items[0].seatId").value(fixture.firstSeat().getId()))
                .andExpect(jsonPath("$.items[1].type").value("SEAT"))
                .andExpect(jsonPath("$.items[1].seatId").value(fixture.secondSeat().getId()));
    }

    @Test
    @DisplayName("HLD-002 expired hold is marked expired and releases resources on lookup")
    void hld002_expiredHold_lookupReleasesResources() throws Exception {
        UserSession session = createUserSession();
        HoldFixture fixture = createFixture();

        ShowSeat showSeat = showSeatRepository.findAllByShowId(fixture.show().getId()).get(0);
        showSeat.hold();

        Hold hold = Hold.create(session.userId(), fixture.show().getId(), LocalDateTime.now().minusMinutes(1));
        hold.addSeatItem(fixture.firstSeat().getId(), showSeat.getPrice());
        holdRepository.save(hold);

        mockMvc.perform(get("/api/holds/{holdId}", hold.getId())
                        .header("Authorization", bearer(session.accessToken()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hold.status").value("EXPIRED"));

        assertThat(showSeatRepository.findAllByShowId(fixture.show().getId()).get(0).getStatus())
                .isEqualTo(ShowSeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("HLD-003 DELETE /api/holds/{holdId} cancels a hold and releases resources")
    void hld003_deleteHold_releasesResources() throws Exception {
        UserSession session = createUserSession();
        HoldFixture fixture = createFixture();
        String holdId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId(), fixture.secondSeat().getId())
        );

        mockMvc.perform(delete("/api/holds/{holdId}", holdId)
                        .header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isNoContent());

        Hold hold = holdRepository.findById(holdId).orElseThrow();
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.CANCELED);
        assertThat(showSeatRepository.findAllByShowId(fixture.show().getId()))
                .extracting(ShowSeat::getStatus)
                .containsOnly(ShowSeatStatus.AVAILABLE);
    }

    private HoldFixture createFixture() {
        Event event = eventRepository.save(Event.create("Hold Test", "Hold flow", EventStatus.PUBLISHED));
        Show show = showRepository.save(Show.schedule(event, LocalDateTime.of(2026, 6, 1, 19, 0), 701L));
        Section section = sectionRepository.save(Section.create("A구역", 701L));
        Seat firstSeat = seatRepository.save(Seat.create("A1", 1, 1, 701L, section));
        Seat secondSeat = seatRepository.save(Seat.create("A2", 1, 2, 701L, section));

        showSeatRepository.save(ShowSeat.createAvailable(show, firstSeat, 150000));
        showSeatRepository.save(ShowSeat.createAvailable(show, secondSeat, 150000));

        return new HoldFixture(show, firstSeat, secondSeat);
    }

    private String createHold(
            String accessToken,
            Long showId,
            List<Long> seatIds
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/holds")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateHoldRequest(
                                showId,
                                seatIds.stream().map(CreateHoldRequest.Item::new).toList()
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("holdId").asText();
    }

    private UserSession createUserSession() {
        String email = "hold" + System.nanoTime() + "@example.com";
        User user = userRepository.save(User.createUser(email, passwordEncoder.encode("password123")));
        createdUserIds.add(user.getId());
        TokenPair tokens = jwtTokenProvider.createTokens(user.getId(), user.getEmail(), user.getRole());
        tokenSessionRepository.save(
                TokenSession.issue(
                        user.getId(),
                        jwtTokenProvider.getTokenId(tokens.accessToken()),
                        tokens.accessToken(),
                        jwtTokenProvider.getTokenId(tokens.refreshToken()),
                        tokens.refreshToken()
                ),
                jwtTokenProvider.getRefreshTokenTtl()
        );
        return new UserSession(user.getId(), tokens.accessToken());
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

    private record HoldFixture(
            Show show,
            Seat firstSeat,
            Seat secondSeat
    ) {
    }

    private record UserSession(
            Long userId,
            String accessToken
    ) {
    }
}
