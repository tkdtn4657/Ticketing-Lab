package com.ticketinglab.checkin.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketinglab.auth.domain.TokenSession;
import com.ticketinglab.auth.domain.TokenSessionRepository;
import com.ticketinglab.auth.infrastructure.jwt.JwtTokenProvider;
import com.ticketinglab.auth.presentation.dto.TokenPair;
import com.ticketinglab.checkin.presentation.dto.CheckinRequest;
import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.hold.presentation.dto.CreateHoldRequest;
import com.ticketinglab.payment.presentation.dto.ConfirmPaymentRequest;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.reservation.presentation.dto.CreateReservationRequest;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.ticket.domain.Ticket;
import com.ticketinglab.ticket.domain.TicketRepository;
import com.ticketinglab.ticket.domain.TicketStatus;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CheckinControllerIntegrationTest {

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
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenSessionRepository tokenSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("CHK-001 POST /api/checkin marks ticket used and GET /api/me/tickets reflects USED")
    void chk001_checkin_marksTicketUsed() throws Exception {
        UserSession userSession = createUserSession("USER");
        String adminToken = createAccessToken("ADMIN");
        CheckinFixture fixture = createFixture();
        Ticket issuedTicket = createIssuedTicket(userSession.accessToken(), fixture);

        mockMvc.perform(post("/api/checkin")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CheckinRequest(issuedTicket.getQrToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(issuedTicket.getId()))
                .andExpect(jsonPath("$.reservationId").value(issuedTicket.getReservationItem().getReservation().getId()))
                .andExpect(jsonPath("$.showId").value(fixture.show().getId()))
                .andExpect(jsonPath("$.status").value("USED"))
                .andExpect(jsonPath("$.usedAt").isNotEmpty());

        Ticket usedTicket = ticketRepository.findAllByReservationId(
                issuedTicket.getReservationItem().getReservation().getId()
        ).get(0);

        assertThat(usedTicket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(usedTicket.getUsedAt()).isNotNull();

        mockMvc.perform(get("/api/me/tickets")
                        .header("Authorization", bearer(userSession.accessToken()))
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets.length()").value(1))
                .andExpect(jsonPath("$.tickets[0].ticketId").value(issuedTicket.getId()))
                .andExpect(jsonPath("$.tickets[0].status").value("USED"))
                .andExpect(jsonPath("$.tickets[0].usedAt").isNotEmpty());
    }

    @Test
    @DisplayName("CHK-002 POST /api/checkin prevents duplicate checkin")
    void chk002_checkin_preventsDuplicateUse() throws Exception {
        UserSession userSession = createUserSession("USER");
        String adminToken = createAccessToken("ADMIN");
        CheckinFixture fixture = createFixture();
        Ticket issuedTicket = createIssuedTicket(userSession.accessToken(), fixture);

        mockMvc.perform(post("/api/checkin")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CheckinRequest(issuedTicket.getQrToken()))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/checkin")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CheckinRequest(issuedTicket.getQrToken()))))
                .andExpect(status().isConflict());

        Ticket usedTicket = ticketRepository.findAllByReservationId(
                issuedTicket.getReservationItem().getReservation().getId()
        ).get(0);

        assertThat(usedTicket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(usedTicket.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("CHK-003 POST /api/checkin returns 404 when qrToken is invalid")
    void chk003_checkin_rejectsInvalidQrToken() throws Exception {
        String adminToken = createAccessToken("ADMIN");

        mockMvc.perform(post("/api/checkin")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CheckinRequest("missing-qr-token"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CHK-004 POST /api/checkin rejects non-admin users")
    void chk004_checkin_requiresAdminRole() throws Exception {
        UserSession userSession = createUserSession("USER");

        mockMvc.perform(post("/api/checkin")
                        .header("Authorization", bearer(userSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CheckinRequest("any-qr-token"))))
                .andExpect(status().isForbidden());
    }

    private CheckinFixture createFixture() {
        Event event = eventRepository.save(Event.create("Checkin Test", "Checkin flow", EventStatus.PUBLISHED));
        Show show = showRepository.save(Show.schedule(event, LocalDateTime.of(2026, 8, 15, 19, 0), 1301L));
        Seat seat = seatRepository.save(Seat.create("C1", 3, 1, 1301L));
        showSeatRepository.save(ShowSeat.createAvailable(show, seat, 99000));
        return new CheckinFixture(show, seat);
    }

    private Ticket createIssuedTicket(String accessToken, CheckinFixture fixture) throws Exception {
        String reservationId = createReservation(accessToken, fixture);
        int amount = reservationRepository.findById(reservationId).orElseThrow().getTotalAmount();

        mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", bearer(accessToken))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new ConfirmPaymentRequest(reservationId, amount))))
                .andExpect(status().isOk());

        List<Ticket> tickets = ticketRepository.findAllByReservationId(reservationId);
        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getStatus()).isEqualTo(TicketStatus.ISSUED);
        return tickets.get(0);
    }

    private String createReservation(String accessToken, CheckinFixture fixture) throws Exception {
        String holdId = createHold(accessToken, fixture);

        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateReservationRequest(holdId))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("reservationId").asText();
    }

    private String createHold(String accessToken, CheckinFixture fixture) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/holds")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateHoldRequest(
                                fixture.show().getId(),
                                List.of(new CreateHoldRequest.Item(fixture.seat().getId(), null, null))
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("holdId").asText();
    }

    private UserSession createUserSession(String role) {
        String accessToken = createAccessToken(role);
        Long userId = Long.valueOf(jwtTokenProvider.getUserId(accessToken));
        return new UserSession(userId, accessToken);
    }

    private String createAccessToken(String role) {
        String email = role.toLowerCase() + System.nanoTime() + "@example.com";
        User user = "ADMIN".equals(role)
                ? userRepository.save(User.createAdmin(email, passwordEncoder.encode("password123")))
                : userRepository.save(User.createUser(email, passwordEncoder.encode("password123")));
        TokenPair tokens = jwtTokenProvider.createTokens(user.getId(), user.getEmail(), user.getRole());
        tokenSessionRepository.save(
                TokenSession.issue(user.getId(), tokens.accessToken(), tokens.refreshToken()),
                jwtTokenProvider.getRefreshTokenTtl()
        );
        return tokens.accessToken();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record CheckinFixture(
            Show show,
            Seat seat
    ) {
    }

    private record UserSession(
            Long userId,
            String accessToken
    ) {
    }
}
