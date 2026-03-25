package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.CreateShowSeatsRequest;
import com.ticketinglab.admin.presentation.dto.CreatedCountResponse;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CreateShowSeatsUseCase {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;

    @Transactional
    public CreatedCountResponse execute(Long showId, CreateShowSeatsRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "show not found"));

        List<Long> seatIds = request.items().stream()
                .map(CreateShowSeatsRequest.Item::seatId)
                .toList();
        validateDistinctSeatIds(seatIds);

        List<Seat> seats = seatRepository.findAllByVenueIdAndIdIn(show.getVenueId(), seatIds);
        if (seats.size() != seatIds.size()) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid seat ids");
        }

        if (!showSeatRepository.findAllByShowIdAndSeatIdIn(showId, seatIds).isEmpty()) {
            throw new ResponseStatusException(CONFLICT, "show seat already exists");
        }

        Map<Long, Seat> seatById = seats.stream()
                .collect(Collectors.toMap(Seat::getId, Function.identity()));

        List<ShowSeat> showSeats = request.items().stream()
                .map(item -> ShowSeat.createAvailable(show, seatById.get(item.seatId()), item.price()))
                .toList();

        showSeatRepository.saveAll(showSeats);
        return new CreatedCountResponse(showSeats.size());
    }

    private void validateDistinctSeatIds(List<Long> seatIds) {
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new ResponseStatusException(CONFLICT, "duplicate seat ids");
        }
    }
}