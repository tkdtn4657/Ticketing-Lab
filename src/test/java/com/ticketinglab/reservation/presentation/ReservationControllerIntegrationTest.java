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
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.show.domain.ShowSeatStatus;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
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
    private ShowSectionInventoryRepository showSectionInventoryRepository;

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

    @Test
    @DisplayName("RES-001 POST /api/reservations converts a hold into reservation")
    void res001_createReservation_convertsHold() throws Exception {
        UserSession session = createUserSession();
        ReservationFixture fixture = createFixture();
        String holdId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                fixture.firstSeat().getId(),
                fixture.firstSection().getId(),
                3
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
        ShowSeat showSeat = showSeatRepository.findAllByShowIdAndSeatIdIn(
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId())
        ).get(0);
        ShowSectionInventory inventory = showSectionInventoryRepository.findAllByShowIdAndSectionIdIn(
                fixture.show().getId(),
                List.of(fixture.firstSection().getId())
        ).get(0);

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.CONVERTED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(reservation.getItems()).hasSize(2);
        assertThat(reservation.getTotalAmount()).isEqualTo(150000 + (3 * 120000));
        assertThat(showSeat.getStatus()).isEqualTo(ShowSeatStatus.RESERVED);
        assertThat(inventory.getHoldQty()).isEqualTo(3);
    }

    @Test
    @DisplayName("RES-002 GET /api/reservations/{reservationId} returns reservation detail")
    void res002_getReservation_returnsDetail() throws Exception {
        UserSession session = createUserSession();
        ReservationFixture fixture = createFixture();
        String holdId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                fixture.firstSeat().getId(),
                fixture.firstSection().getId(),
                2
        );
        String reservationId = createReservation(session.accessToken(), holdId);

        mockMvc.perform(get("/api/reservations/{reservationId}", reservationId)
                        .header("Authorization", bearer(session.accessToken()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservation.reservationId").value(reservationId))
                .andExpect(jsonPath("$.reservation.showId").value(fixture.show().getId()))
                .andExpect(jsonPath("$.reservation.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.reservation.totalAmount").value(390000))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].type").value("SEAT"))
                .andExpect(jsonPath("$.items[0].seatId").value(fixture.firstSeat().getId()))
                .andExpect(jsonPath("$.items[1].type").value("SECTION"))
                .andExpect(jsonPath("$.items[1].sectionId").value(fixture.firstSection().getId()))
                .andExpect(jsonPath("$.items[1].qty").value(2));
    }

    @Test
    @DisplayName("RES-003 GET /api/me/reservations expires stale reservations before listing")
    void res003_listMyReservations_expiresStaleReservations() throws Exception {
        UserSession session = createUserSession();
        ReservationFixture fixture = createFixture();

        String activeHoldId = createHold(
                session.accessToken(),
                fixture.show().getId(),
                fixture.firstSeat().getId(),
                fixture.firstSection().getId(),
                2
        );
        createReservation(session.accessToken(), activeHoldId);

        String expiredReservationId = createExpiredReservation(
                session.userId(),
                fixture.show(),
                fixture.secondSeat(),
                fixture.secondSection(),
                1
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
        ShowSectionInventory releasedInventory = showSectionInventoryRepository.findAllByShowIdAndSectionIdIn(
                fixture.show().getId(),
                List.of(fixture.secondSection().getId())
        ).get(0);

        assertThat(expiredReservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(releasedSeat.getStatus()).isEqualTo(ShowSeatStatus.AVAILABLE);
        assertThat(releasedInventory.getHoldQty()).isZero();
    }

    @Test
    @DisplayName("expired reservation resources are released when creating a new hold")
    void expiredReservation_releasedOnCreateHold() throws Exception {
        UserSession reservationOwner = createUserSession();
        UserSession newHolder = createUserSession();
        ReservationFixture fixture = createFixture();

        createExpiredReservation(
                reservationOwner.userId(),
                fixture.show(),
                fixture.firstSeat(),
                fixture.firstSection(),
                2
        );

        mockMvc.perform(post("/api/holds")
                        .header("Authorization", bearer(newHolder.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateHoldRequest(
                                fixture.show().getId(),
                                List.of(
                                        new CreateHoldRequest.Item(fixture.firstSeat().getId(), null, null),
                                        new CreateHoldRequest.Item(null, fixture.firstSection().getId(), 2)
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").isString());

        ShowSeat showSeat = showSeatRepository.findAllByShowIdAndSeatIdIn(
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId())
        ).get(0);
        ShowSectionInventory inventory = showSectionInventoryRepository.findAllByShowIdAndSectionIdIn(
                fixture.show().getId(),
                List.of(fixture.firstSection().getId())
        ).get(0);

        assertThat(showSeat.getStatus()).isEqualTo(ShowSeatStatus.HELD);
        assertThat(inventory.getHoldQty()).isEqualTo(2);
    }

    private ReservationFixture createFixture() {
        Event event = eventRepository.save(Event.create("Reservation Test", "Reservation flow", EventStatus.PUBLISHED));
        Show show = showRepository.save(Show.schedule(event, LocalDateTime.of(2026, 7, 1, 19, 0), 801L));
        Seat firstSeat = seatRepository.save(Seat.create("A1", 1, 1, 801L));
        Seat secondSeat = seatRepository.save(Seat.create("A2", 1, 2, 801L));
        Section firstSection = sectionRepository.save(Section.create("VIP", 801L));
        Section secondSection = sectionRepository.save(Section.create("R", 801L));

        showSeatRepository.save(ShowSeat.createAvailable(show, firstSeat, 150000));
        showSeatRepository.save(ShowSeat.createAvailable(show, secondSeat, 140000));
        showSectionInventoryRepository.save(ShowSectionInventory.open(show, firstSection, 120000, 100));
        showSectionInventoryRepository.save(ShowSectionInventory.open(show, secondSection, 90000, 100));

        return new ReservationFixture(show, firstSeat, secondSeat, firstSection, secondSection);
    }

    private String createHold(
            String accessToken,
            Long showId,
            Long seatId,
            Long sectionId,
            int sectionQty
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/holds")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateHoldRequest(
                                showId,
                                List.of(
                                        new CreateHoldRequest.Item(seatId, null, null),
                                        new CreateHoldRequest.Item(null, sectionId, sectionQty)
                                )
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
            Seat seat,
            Section section,
            int sectionQty
    ) {
        ShowSeat showSeat = showSeatRepository.findAllByShowIdAndSeatIdIn(show.getId(), List.of(seat.getId())).get(0);
        ShowSectionInventory inventory = showSectionInventoryRepository.findAllByShowIdAndSectionIdIn(
                show.getId(),
                List.of(section.getId())
        ).get(0);

        showSeat.hold();
        showSeat.reserve();
        inventory.hold(sectionQty);

        Hold hold = Hold.create(userId, show.getId(), LocalDateTime.now().plusMinutes(5));
        hold.addSeatItem(seat.getId(), showSeat.getPrice());
        hold.addSectionItem(section.getId(), sectionQty, inventory.getPrice());
        hold.convert();
        holdRepository.save(hold);

        Reservation reservation = Reservation.createFromHold(hold, LocalDateTime.now().minusMinutes(1));
        return reservationRepository.save(reservation).getId();
    }

    private UserSession createUserSession() {
        String email = "reservation" + System.nanoTime() + "@example.com";
        User user = userRepository.save(User.createUser(email, passwordEncoder.encode("password123")));
        TokenPair tokens = jwtTokenProvider.createTokens(user.getId(), user.getEmail(), user.getRole());
        tokenSessionRepository.save(
                TokenSession.issue(user.getId(), tokens.accessToken(), tokens.refreshToken()),
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
            Seat secondSeat,
            Section firstSection,
            Section secondSection
    ) {
    }

    private record UserSession(
            Long userId,
            String accessToken
    ) {
    }
}
