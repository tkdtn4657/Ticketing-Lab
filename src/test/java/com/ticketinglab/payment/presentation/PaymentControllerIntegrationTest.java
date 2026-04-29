package com.ticketinglab.payment.presentation;

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
import com.ticketinglab.hold.presentation.dto.CreateHoldRequest;
import com.ticketinglab.payment.domain.Payment;
import com.ticketinglab.payment.domain.PaymentRepository;
import com.ticketinglab.payment.presentation.dto.ConfirmPaymentRequest;
import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.reservation.domain.ReservationStatus;
import com.ticketinglab.reservation.presentation.dto.CreateReservationRequest;
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.show.domain.ShowSeatStatus;
import com.ticketinglab.ticket.domain.Ticket;
import com.ticketinglab.ticket.domain.TicketRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

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
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

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
    @DisplayName("PAY-001 POST /api/payments/confirm marks reservation paid and issues tickets")
    void pay001_confirmPayment_marksReservationPaidAndIssuesTickets() throws Exception {
        UserSession session = createUserSession();
        PaymentFixture fixture = createFixture();
        String reservationId = createReservation(session.accessToken(), fixture, 2);
        int amount = reservationRepository.findById(reservationId).orElseThrow().getTotalAmount();
        String idempotencyKey = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", bearer(session.accessToken()))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new ConfirmPaymentRequest(reservationId, amount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").isNumber())
                .andExpect(jsonPath("$.reservationId").value(reservationId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reservationStatus").value("PAID"));

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        Payment payment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
        List<Ticket> tickets = ticketRepository.findAllByReservationId(reservationId);
        ShowSeat showSeat = showSeatRepository.findAllByShowIdAndSeatIdIn(
                fixture.show().getId(),
                List.of(fixture.firstSeat().getId())
        ).get(0);
        ShowSectionInventory inventory = showSectionInventoryRepository.findAllByShowIdAndSectionIdIn(
                fixture.show().getId(),
                List.of(fixture.firstSection().getId())
        ).get(0);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(reservation.getExpiresAt()).isNull();
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getReservation().getId()).isEqualTo(reservationId);
        assertThat(tickets).hasSize(3);
        assertThat(showSeat.getStatus()).isEqualTo(ShowSeatStatus.RESERVED);
        assertThat(inventory.getHoldQty()).isZero();
        assertThat(inventory.getSoldQty()).isEqualTo(2);
    }

    @Test
    @DisplayName("PAY-002 POST /api/payments/confirm returns same response for same idempotency key")
    void pay002_confirmPayment_isIdempotent() throws Exception {
        UserSession session = createUserSession();
        PaymentFixture fixture = createFixture();
        String reservationId = createReservation(session.accessToken(), fixture, 1);
        int amount = reservationRepository.findById(reservationId).orElseThrow().getTotalAmount();
        String idempotencyKey = UUID.randomUUID().toString();

        MvcResult firstResult = mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", bearer(session.accessToken()))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new ConfirmPaymentRequest(reservationId, amount))))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult secondResult = mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", bearer(session.accessToken()))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new ConfirmPaymentRequest(reservationId, amount))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reservationStatus").value("PAID"))
                .andReturn();

        JsonNode firstBody = body(firstResult);
        JsonNode secondBody = body(secondResult);
        List<Ticket> tickets = ticketRepository.findAllByReservationId(reservationId);

        assertThat(secondBody.get("paymentId").asLong()).isEqualTo(firstBody.get("paymentId").asLong());
        assertThat(tickets).hasSize(2);
    }

    @Test
    @DisplayName("PAY-003 POST /api/payments/confirm rejects amount mismatch")
    void pay003_confirmPayment_rejectsAmountMismatch() throws Exception {
        UserSession session = createUserSession();
        PaymentFixture fixture = createFixture();
        String reservationId = createReservation(session.accessToken(), fixture, 1);
        int amount = reservationRepository.findById(reservationId).orElseThrow().getTotalAmount();

        mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", bearer(session.accessToken()))
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new ConfirmPaymentRequest(reservationId, amount - 1))))
                .andExpect(status().isConflict());

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        List<Ticket> tickets = ticketRepository.findAllByReservationId(reservationId);
        ShowSectionInventory inventory = showSectionInventoryRepository.findAllByShowIdAndSectionIdIn(
                fixture.show().getId(),
                List.of(fixture.firstSection().getId())
        ).get(0);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(tickets).isEmpty();
        assertThat(inventory.getHoldQty()).isEqualTo(1);
        assertThat(inventory.getSoldQty()).isZero();
    }

    @Test
    @DisplayName("TKT-001 GET /api/me/tickets returns issued tickets after payment")
    void tkt001_listMyTickets_returnsIssuedTickets() throws Exception {
        UserSession session = createUserSession();
        PaymentFixture fixture = createFixture();
        String reservationId = createReservation(session.accessToken(), fixture, 2);
        int amount = reservationRepository.findById(reservationId).orElseThrow().getTotalAmount();

        confirmPayment(session.accessToken(), reservationId, amount, UUID.randomUUID().toString());

        mockMvc.perform(get("/api/me/tickets")
                        .header("Authorization", bearer(session.accessToken()))
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.tickets.length()").value(3))
                .andExpect(jsonPath("$.tickets[0].reservationId").value(reservationId))
                .andExpect(jsonPath("$.tickets[0].showId").value(fixture.show().getId()))
                .andExpect(jsonPath("$.tickets[0].status").value("ISSUED"));
    }

    private PaymentFixture createFixture() {
        Event event = eventRepository.save(Event.create("Payment Test", "Payment flow", EventStatus.PUBLISHED));
        Show show = showRepository.save(Show.schedule(event, LocalDateTime.of(2026, 7, 1, 19, 0), 901L));
        Seat firstSeat = seatRepository.save(Seat.create("A1", 1, 1, 901L));
        Section firstSection = sectionRepository.save(Section.create("VIP", 901L));

        showSeatRepository.save(ShowSeat.createAvailable(show, firstSeat, 150000));
        showSectionInventoryRepository.save(ShowSectionInventory.open(show, firstSection, 120000, 100));

        return new PaymentFixture(show, firstSeat, firstSection);
    }

    private String createReservation(String accessToken, PaymentFixture fixture, int sectionQty) throws Exception {
        String holdId = createHold(accessToken, fixture, sectionQty);

        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateReservationRequest(holdId))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("reservationId").asText();
    }

    private String createHold(String accessToken, PaymentFixture fixture, int sectionQty) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/holds")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new CreateHoldRequest(
                                fixture.show().getId(),
                                List.of(
                                        new CreateHoldRequest.Item(fixture.firstSeat().getId(), null, null),
                                        new CreateHoldRequest.Item(null, fixture.firstSection().getId(), sectionQty)
                                )
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return body(result).get("holdId").asText();
    }

    private void confirmPayment(String accessToken, String reservationId, int amount, String idempotencyKey) throws Exception {
        mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", bearer(accessToken))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json(new ConfirmPaymentRequest(reservationId, amount))))
                .andExpect(status().isOk());
    }

    private UserSession createUserSession() {
        String email = "payment" + System.nanoTime() + "@example.com";
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

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record PaymentFixture(
            Show show,
            Seat firstSeat,
            Section firstSection
    ) {
    }

    private record UserSession(
            Long userId,
            String accessToken
    ) {
    }
}
