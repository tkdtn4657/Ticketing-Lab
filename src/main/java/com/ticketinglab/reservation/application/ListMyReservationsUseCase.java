package com.ticketinglab.reservation.application;

import com.ticketinglab.reservation.domain.Reservation;
import com.ticketinglab.reservation.domain.ReservationRepository;
import com.ticketinglab.reservation.domain.ReservationStatus;
import com.ticketinglab.reservation.presentation.dto.MyReservationListResponse;
import com.ticketinglab.reservation.presentation.dto.ReservationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class ListMyReservationsUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationResourceManager reservationResourceManager;

    @Transactional
    public MyReservationListResponse execute(Long userId, int page, int size, String rawStatus) {
        reservationResourceManager.expirePendingReservationsOfUser(userId, LocalDateTime.now());

        ReservationStatus status = resolveStatus(rawStatus);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<Reservation> reservations = status == null
                ? reservationRepository.findPageByUserId(userId, pageable)
                : reservationRepository.findPageByUserIdAndStatus(userId, status, pageable);

        List<ReservationSummaryResponse> responses = reservations.getContent().stream()
                .map(ReservationSummaryResponse::from)
                .toList();

        return new MyReservationListResponse(
                reservations.getNumber(),
                reservations.getSize(),
                reservations.getTotalElements(),
                reservations.getTotalPages(),
                responses
        );
    }

    private ReservationStatus resolveStatus(String rawStatus) {
        if (!StringUtils.hasText(rawStatus)) {
            return null;
        }

        try {
            return ReservationStatus.from(rawStatus);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid reservation status");
        }
    }
}