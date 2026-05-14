package com.ticketinglab.reservation.presentation;

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
import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.reservation.domain.ReservationStatus;
import com.ticketinglab.reservation.presentation.dto.CreateReservationRequest;
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
class ReservationControllerIntegrationTest {

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
    private ReservationRepository reservationRepository;

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
    @DisplayName("RES-001 POST /api/reservations converts a seat hold into reservation")
    void res001_createReservation_convertsHold() throws Exception {
        UserSession session = createUserSession();
        ReservationFixture fixture = createFixture();
        String holdId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId(), fixture.secondSeat().getId())
        );

        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(session.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateReservationRequest(holdId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").isString())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn();

        String reservationId = body(result).get("reservationId").asText();
        Hold hold = holdRepository.findById(holdId).orElseThrow();
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.CONVERTED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(reservation.getItems()).hasSize(2);
        assertThat(reservation.getTotalAmount()).isEqualTo(290000);
        assertThat(showSeatStatuses(fixture)).containsOnly(ShowSeatStatus.RESERVED);
    }

    @Test
    @DisplayName("RES-004 DELETE /api/reservations/{reservationId} cancels pending reservation and releases seats")
    void res004_cancelReservation_releasesSeats() throws Exception {
        UserSession session = createUserSession();
        ReservationFixture fixture = createFixture();
        String holdId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId(), fixture.secondSeat().getId())
        );
        String reservationId = createReservation(session.accessToken(), holdId);

        mockMvc.perform(delete("/api/reservations/{reservationId}", reservationId)
                        .header("Authorization", bearer(session.accessToken())))
                .andExpect(status().isNoContent());

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(showSeatStatuses(fixture)).containsOnly(ShowSeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("RES-002 GET /api/reservations/{reservationId} returns seat reservation detail")
    void res002_getReservation_returnsDetail() throws Exception {
        UserSession session = createUserSession();
        ReservationFixture fixture = createFixture();
        String holdId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId(), fixture.secondSeat().getId())
        );
        String reservationId = createReservation(session.accessToken(), holdId);

        mockMvc.perform(get("/api/reservations/{reservationId}", reservationId)
                        .header("Authorization", bearer(session.accessToken()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservation.reservationId").value(reservationId))
                .andExpect(jsonPath("$.reservation.showId").value(fixture.show().getId()))
                .andExpect(jsonPath("$.reservation.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.reservation.totalAmount").value(290000))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].type").value("SEAT"))
                .andExpect(jsonPath("$.items[0].seatId").value(fixture.firstSeat().getId()))
                .andExpect(jsonPath("$.items[1].type").value("SEAT"))
                .andExpect(jsonPath("$.items[1].seatId").value(fixture.secondSeat().getId()));
    }

    @Test
    @DisplayName("RES-003 GET /api/me/reservations expires stale reservations before listing")
    void res003_listMyReservations_expiresStaleReservations() throws Exception {
        UserSession session = createUserSession();
        ReservationFixture fixture = createFixture();

        String activeHoldId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId())
        );
        createReservation(session.accessToken(), activeHoldId);

        String expiredReservationId = createExpiredReservation(
                session.userId(),
                fixture.show(),
                fixture.secondSeat()
        );

        mockMvc.perform(get("/api/me/reservations")
                        .header("Authorization", bearer(session.accessToken()))
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .queryParam("status", "EXPIRED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.reservations.length()").value(1))
                .andExpect(jsonPath("$.reservations[0].reservationId").value(expiredReservationId))
                .andExpect(jsonPath("$.reservations[0].status").value("EXPIRED"));

        Reservation expiredReservation = reservationRepository.findById(expiredReservationId).orElseThrow();
        ShowSeat releasedSeat = showSeatRepository.findAllByShowIdAndSeatIdIn(
                fixture.show().getId(),
                List.of(fixture.secondSeat().getId())
        ).get(0);

        assertThat(expiredReservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(releasedSeat.getStatus()).isEqualTo(ShowSeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("expired reservation seat is released when creating a new hold")
    void expiredReservation_releasedOnCreateHold() throws Exception {
        UserSession reservationOwner = createUserSession();
        UserSession newHolder = createUserSession();
        ReservationFixture fixture = createFixture();

        createExpiredReservation(
                reservationOwner.userId(),
                fixture.show(),
                fixture.firstSeat()
        );

        mockMvc.perform(post("/api/holds")
                        .header("Authorization", bearer(newHolder.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateHoldRequest(
                                fixture.show().getId(),
                                List.of(new CreateHoldRequest.Item(fixture.firstSeat().getId()))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").isString());

        ShowSeat showSeat = showSeatRepository.findAllByShowIdAndSeatIdIn(
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId())
        ).get(0);

        assertThat(showSeat.getStatus()).isEqualTo(ShowSeatStatus.HELD);
    }

    private ReservationFixture createFixture() {
        Event event = eventRepository.save(Event.create("Reservation Test", "Reservation flow", EventStatus.PUBLISHED));
        Show show = showRepository.save(Show.schedule(event, LocalDateTime.of(2026, 7, 1, 19, 0), 801L));
        Section section = sectionRepository.save(Section.create("A구역", 801L));
        Seat firstSeat = seatRepository.save(Seat.create("A1", 1, 1, 801L, section));
        Seat secondSeat = seatRepository.save(Seat.create("A2", 1, 2, 801L, section));

        showSeatRepository.save(ShowSeat.createAvailable(show, firstSeat, 150000));
        showSeatRepository.save(ShowSeat.createAvailable(show, secondSeat, 140000));

        return new ReservationFixture(show, firstSeat, secondSeat);
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

    private String createReservation(String accessToken, String holdId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateReservationRequest(holdId))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("reservationId").asText();
    }

    private String createExpiredReservation(
            Long userId,
            Show show,
            Seat seat
    ) {
        ShowSeat showSeat = showSeatRepository.findAllByShowIdAndSeatIdIn(show.getId(), List.of(seat.getId())).get(0);

        showSeat.hold();
        showSeat.reserve();

        Hold hold = Hold.create(userId, show.getId(), LocalDateTime.now().plusMinutes(5));
        hold.addSeatItem(seat.getId(), showSeat.getPrice());
        hold.convert();
        holdRepository.save(hold);

        Reservation reservation = Reservation.createFromHold(hold, LocalDateTime.now().minusMinutes(1));
        return reservationRepository.save(reservation).getId();
    }

    private List<ShowSeatStatus> showSeatStatuses(ReservationFixture fixture) {
        return showSeatRepository.findAllByShowIdAndSeatIdIn(
                        fixture.show().getId(),
                        List.of(fixture.firstSeat().getId(), fixture.secondSeat().getId())
                )
                .stream()
                .map(ShowSeat::getStatus)
                .toList();
    }

    private UserSession createUserSession() {
        String email = "reservation" + System.nanoTime() + "@example.com";
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

    private record ReservationFixture(
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
