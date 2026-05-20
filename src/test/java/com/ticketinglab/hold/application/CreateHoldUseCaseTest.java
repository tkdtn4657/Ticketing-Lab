package com.ticketinglab.hold.application;

import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.hold.presentation.dto.CreateHoldRequest;
import com.ticketinglab.reservation.application.ReservationResourceManager;
import com.ticketinglab.show.domain.ShowSeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;

@ExtendWith(MockitoExtension.class)
class CreateHoldUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long SHOW_ID = 10L;
    private static final Long SEAT_ID = 100L;
    private static final Duration PRE_LOCK_TTL = Duration.ofSeconds(60);
    private static final Duration SEAT_QUEUE_TTL = Duration.ofSeconds(30);

    @Mock
    private ShowRepository showRepository;

    @Mock
    private HoldRepository holdRepository;

    @Mock
    private HoldResourceManager holdResourceManager;

    @Mock
    private ReservationResourceManager reservationResourceManager;

    @Mock
    private SeatHoldPreLockManager seatHoldPreLockManager;

    @Mock
    private SeatHoldQueueManager seatHoldQueueManager;

    private CreateHoldUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateHoldUseCase(
                showRepository,
                holdRepository,
                holdResourceManager,
                reservationResourceManager,
                seatHoldPreLockManager,
                seatHoldQueueManager
        );
        ReflectionTestUtils.setField(useCase, "holdTtlMinutes", 5L);
        ReflectionTestUtils.setField(useCase, "preLockTtl", PRE_LOCK_TTL);
        ReflectionTestUtils.setField(useCase, "seatQueueTtl", SEAT_QUEUE_TTL);
    }

    @Test
    @DisplayName("좌석별 큐가 가득 차면 pre-lock과 DB 자원 잠금 전에 409로 실패한다")
    void execute_queueFull_shortCircuitsBeforePreLockAndDatabaseResourceLock() {
        CreateHoldRequest request = request(SEAT_ID);

        when(seatHoldQueueManager.tryEnter(eq(SHOW_ID), eq(Set.of(SEAT_ID)), eq(USER_ID), eq(SEAT_QUEUE_TTL)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(CONFLICT));

        verify(seatHoldPreLockManager, never()).acquire(any(), any(), any());
        verify(showRepository, never()).findById(any());
        verify(reservationResourceManager, never()).expirePendingReservations(any(), any(), any());
        verify(holdResourceManager, never()).prepareForCreate(any(), any(), any());
        verify(holdRepository, never()).save(any());
    }

    @Test
    @DisplayName("Redis pre-lock 충돌이면 DB 자원 잠금 전에 409로 실패한다")
    void execute_preLockConflict_shortCircuitsBeforeDatabaseResourceLock() {
        CreateHoldRequest request = request(SEAT_ID);
        SeatHoldQueueTicket queueTicket = queueTicket();

        stubQueueEnter(queueTicket);
        when(seatHoldPreLockManager.acquire(eq(SHOW_ID), eq(Set.of(SEAT_ID)), eq(PRE_LOCK_TTL)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(CONFLICT));

        verify(showRepository, never()).findById(any());
        verify(reservationResourceManager, never()).expirePendingReservations(any(), any(), any());
        verify(holdResourceManager, never()).prepareForCreate(any(), any(), any());
        verify(holdRepository, never()).save(any());
        verify(seatHoldQueueManager).leave(queueTicket);
    }

    @Test
    @DisplayName("Redis pre-lock 이후 show 검증에 실패하면 attempt pre-lock을 정리한다")
    void execute_showNotFound_releasesAttemptPreLock() {
        CreateHoldRequest request = request(SEAT_ID);
        SeatHoldQueueTicket queueTicket = queueTicket();
        SeatHoldPreLock preLock = SeatHoldPreLock.acquire(SHOW_ID, List.of(SEAT_ID), "attempt:test");

        stubQueueEnter(queueTicket);
        when(seatHoldPreLockManager.acquire(eq(SHOW_ID), eq(Set.of(SEAT_ID)), eq(PRE_LOCK_TTL)))
                .thenReturn(Optional.of(preLock));
        when(showRepository.findById(SHOW_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_ID, request))
                .isInstanceOf(ResponseStatusException.class);

        verify(seatHoldPreLockManager).release(preLock);
        verify(reservationResourceManager, never()).expirePendingReservations(any(), any(), any());
        verify(holdResourceManager, never()).prepareForCreate(any(), any(), any());
        verify(seatHoldQueueManager).leave(queueTicket);
    }

    @Test
    @DisplayName("DB 처리 중 실패하면 Redis attempt pre-lock을 정리한다")
    void execute_databaseFailure_releasesAttemptPreLock() {
        Show show = show();
        CreateHoldRequest request = request(SEAT_ID);
        SeatHoldQueueTicket queueTicket = queueTicket();
        SeatHoldPreLock preLock = SeatHoldPreLock.acquire(SHOW_ID, List.of(SEAT_ID), "attempt:test");

        stubQueueEnter(queueTicket);
        when(showRepository.findById(SHOW_ID)).thenReturn(Optional.of(show));
        when(seatHoldPreLockManager.acquire(eq(SHOW_ID), eq(Set.of(SEAT_ID)), eq(PRE_LOCK_TTL)))
                .thenReturn(Optional.of(preLock));
        when(holdResourceManager.prepareForCreate(eq(SHOW_ID), eq(Set.of(SEAT_ID)), any()))
                .thenThrow(new ResponseStatusException(CONFLICT, "seat not available"));

        assertThatThrownBy(() -> useCase.execute(USER_ID, request))
                .isInstanceOf(ResponseStatusException.class);

        verify(seatHoldPreLockManager).release(preLock);
        verify(seatHoldQueueManager).leave(queueTicket);
    }

    @Test
    @DisplayName("Hold 생성 성공 시 Redis pre-lock owner를 holdId로 확정한다")
    void execute_success_confirmsPreLockWithHoldId() {
        Show show = show();
        ShowSeat showSeat = showSeat();
        CreateHoldRequest request = request(SEAT_ID);
        SeatHoldQueueTicket queueTicket = queueTicket();
        SeatHoldPreLock preLock = SeatHoldPreLock.acquire(SHOW_ID, List.of(SEAT_ID), "attempt:test");

        stubQueueEnter(queueTicket);
        when(showRepository.findById(SHOW_ID)).thenReturn(Optional.of(show));
        when(seatHoldPreLockManager.acquire(eq(SHOW_ID), eq(Set.of(SEAT_ID)), eq(PRE_LOCK_TTL)))
                .thenReturn(Optional.of(preLock));
        when(holdResourceManager.prepareForCreate(eq(SHOW_ID), eq(Set.of(SEAT_ID)), any()))
                .thenReturn(new HoldResourceManager.LockedResources(Map.of(SEAT_ID, showSeat)));
        when(holdRepository.save(any(Hold.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(USER_ID, request);

        var inOrder = inOrder(seatHoldQueueManager, seatHoldPreLockManager);
        inOrder.verify(seatHoldQueueManager).tryEnter(eq(SHOW_ID), eq(Set.of(SEAT_ID)), eq(USER_ID), eq(SEAT_QUEUE_TTL));
        inOrder.verify(seatHoldPreLockManager).acquire(eq(SHOW_ID), eq(Set.of(SEAT_ID)), eq(PRE_LOCK_TTL));
        verify(seatHoldPreLockManager).confirmHold(eq(preLock), any(), eq(PRE_LOCK_TTL));
        verify(seatHoldQueueManager).leave(queueTicket);
    }

    private Show show() {
        Show show = org.mockito.Mockito.mock(Show.class);
        when(show.getId()).thenReturn(SHOW_ID);
        return show;
    }

    private ShowSeat showSeat() {
        ShowSeat showSeat = org.mockito.Mockito.mock(ShowSeat.class);
        when(showSeat.getPrice()).thenReturn(150000);
        return showSeat;
    }

    private CreateHoldRequest request(Long seatId) {
        return new CreateHoldRequest(SHOW_ID, List.of(new CreateHoldRequest.Item(seatId)));
    }

    private void stubQueueEnter(SeatHoldQueueTicket queueTicket) {
        when(seatHoldQueueManager.tryEnter(eq(SHOW_ID), eq(Set.of(SEAT_ID)), eq(USER_ID), eq(SEAT_QUEUE_TTL)))
                .thenReturn(Optional.of(queueTicket));
    }

    private SeatHoldQueueTicket queueTicket() {
        return new SeatHoldQueueTicket(SHOW_ID, Set.of(SEAT_ID), "queue:test");
    }
}
