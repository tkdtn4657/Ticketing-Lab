package com.ticketinglab.admin.presentation;

import com.ticketinglab.admin.application.CreateEventUseCase;
import com.ticketinglab.admin.application.CreateShowSectionInventoriesUseCase;
import com.ticketinglab.admin.application.CreateShowSeatsUseCase;
import com.ticketinglab.admin.application.CreateShowUseCase;
import com.ticketinglab.admin.application.ListVenueSectionsUseCase;
import com.ticketinglab.admin.application.ListVenueSeatsUseCase;
import com.ticketinglab.admin.application.RegisterVenueSectionsUseCase;
import com.ticketinglab.admin.application.RegisterVenueSeatsUseCase;
import com.ticketinglab.admin.application.UpsertVenueUseCase;
import com.ticketinglab.admin.presentation.dto.CreateEventRequest;
import com.ticketinglab.admin.presentation.dto.CreateEventResponse;
import com.ticketinglab.admin.presentation.dto.CreateShowRequest;
import com.ticketinglab.admin.presentation.dto.CreateShowResponse;
import com.ticketinglab.admin.presentation.dto.CreateShowSectionInventoriesRequest;
import com.ticketinglab.admin.presentation.dto.CreateShowSeatsRequest;
import com.ticketinglab.admin.presentation.dto.CreatedCountResponse;
import com.ticketinglab.admin.presentation.dto.RegisterVenueSectionsRequest;
import com.ticketinglab.admin.presentation.dto.RegisterVenueSeatsRequest;
import com.ticketinglab.admin.presentation.dto.VenueSectionListResponse;
import com.ticketinglab.admin.presentation.dto.VenueSeatListResponse;
import com.ticketinglab.admin.presentation.dto.VenueUpsertRequest;
import com.ticketinglab.admin.presentation.dto.VenueUpsertResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UpsertVenueUseCase upsertVenueUseCase;
    private final ListVenueSeatsUseCase listVenueSeatsUseCase;
    private final RegisterVenueSeatsUseCase registerVenueSeatsUseCase;
    private final ListVenueSectionsUseCase listVenueSectionsUseCase;
    private final RegisterVenueSectionsUseCase registerVenueSectionsUseCase;
    private final CreateEventUseCase createEventUseCase;
    private final CreateShowUseCase createShowUseCase;
    private final CreateShowSeatsUseCase createShowSeatsUseCase;
    private final CreateShowSectionInventoriesUseCase createShowSectionInventoriesUseCase;

    @PostMapping("/venues/upsert")
    public ResponseEntity<VenueUpsertResponse> upsertVenue(@Valid @RequestBody VenueUpsertRequest request) {
        return ResponseEntity.ok(upsertVenueUseCase.execute(request));
    }

    @GetMapping("/venues/{venueId}/seats")
    public ResponseEntity<VenueSeatListResponse> listSeats(@PathVariable Long venueId) {
        return ResponseEntity.ok(listVenueSeatsUseCase.execute(venueId));
    }

    @PostMapping("/venues/{venueId}/seats")
    public ResponseEntity<CreatedCountResponse> createSeats(
            @PathVariable Long venueId,
            @Valid @RequestBody RegisterVenueSeatsRequest request
    ) {
        return ResponseEntity.ok(registerVenueSeatsUseCase.execute(venueId, request));
    }

    @GetMapping("/venues/{venueId}/sections")
    public ResponseEntity<VenueSectionListResponse> listSections(@PathVariable Long venueId) {
        return ResponseEntity.ok(listVenueSectionsUseCase.execute(venueId));
    }

    @PostMapping("/venues/{venueId}/sections")
    public ResponseEntity<CreatedCountResponse> createSections(
            @PathVariable Long venueId,
            @Valid @RequestBody RegisterVenueSectionsRequest request
    ) {
        return ResponseEntity.ok(registerVenueSectionsUseCase.execute(venueId, request));
    }

    @PostMapping("/events")
    public ResponseEntity<CreateEventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.ok(createEventUseCase.execute(request));
    }

    @PostMapping("/shows")
    public ResponseEntity<CreateShowResponse> createShow(@Valid @RequestBody CreateShowRequest request) {
        return ResponseEntity.ok(createShowUseCase.execute(request));
    }

    @PostMapping("/shows/{showId}/show-seats")
    public ResponseEntity<CreatedCountResponse> createShowSeats(
            @PathVariable Long showId,
            @Valid @RequestBody CreateShowSeatsRequest request
    ) {
        return ResponseEntity.ok(createShowSeatsUseCase.execute(showId, request));
    }

    @PostMapping("/shows/{showId}/section-inventories")
    public ResponseEntity<CreatedCountResponse> createShowSectionInventories(
            @PathVariable Long showId,
            @Valid @RequestBody CreateShowSectionInventoriesRequest request
    ) {
        return ResponseEntity.ok(createShowSectionInventoriesUseCase.execute(showId, request));
    }
}