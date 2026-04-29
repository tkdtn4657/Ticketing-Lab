package com.ticketinglab.admin.presentation;

import com.ticketinglab.admin.application.ListAdminEventsUseCase;
import com.ticketinglab.admin.application.ListAdminShowsUseCase;
import com.ticketinglab.admin.application.ListAdminVenuesUseCase;
import com.ticketinglab.admin.application.CreateEventUseCase;
import com.ticketinglab.admin.application.CreateShowSectionInventoriesUseCase;
import com.ticketinglab.admin.application.CreateShowSeatsUseCase;
import com.ticketinglab.admin.application.CreateShowUseCase;
import com.ticketinglab.admin.application.ListVenueSectionsUseCase;
import com.ticketinglab.admin.application.ListVenueSeatsUseCase;
import com.ticketinglab.admin.application.RegisterVenueSectionsUseCase;
import com.ticketinglab.admin.application.RegisterVenueSeatsUseCase;
import com.ticketinglab.admin.application.UpsertVenueUseCase;
import com.ticketinglab.admin.presentation.dto.AdminEventListResponse;
import com.ticketinglab.admin.presentation.dto.AdminShowListResponse;
import com.ticketinglab.admin.presentation.dto.AdminVenueListResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController implements AdminApiDocs {

    private final UpsertVenueUseCase upsertVenueUseCase;
    private final ListAdminVenuesUseCase listAdminVenuesUseCase;
    private final ListVenueSeatsUseCase listVenueSeatsUseCase;
    private final RegisterVenueSeatsUseCase registerVenueSeatsUseCase;
    private final ListVenueSectionsUseCase listVenueSectionsUseCase;
    private final RegisterVenueSectionsUseCase registerVenueSectionsUseCase;
    private final ListAdminEventsUseCase listAdminEventsUseCase;
    private final CreateEventUseCase createEventUseCase;
    private final ListAdminShowsUseCase listAdminShowsUseCase;
    private final CreateShowUseCase createShowUseCase;
    private final CreateShowSeatsUseCase createShowSeatsUseCase;
    private final CreateShowSectionInventoriesUseCase createShowSectionInventoriesUseCase;

    @PostMapping("/venues/upsert")
    @Override
    public ResponseEntity<VenueUpsertResponse> upsertVenue(
            Authentication authentication,
            @Valid @RequestBody VenueUpsertRequest request
    ) {
        return ResponseEntity.ok(upsertVenueUseCase.execute(Long.valueOf(authentication.getName()), request));
    }

    @GetMapping("/venues")
    @Override
    public ResponseEntity<AdminVenueListResponse> listVenues(
            Authentication authentication
    ) {
        return ResponseEntity.ok(AdminVenueListResponse.from(
                listAdminVenuesUseCase.execute(Long.valueOf(authentication.getName()))
        ));
    }

    @GetMapping("/venues/{venueId}/seats")
    @Override
    public ResponseEntity<VenueSeatListResponse> listSeats(
            @PathVariable Long venueId
    ) {
        return ResponseEntity.ok(listVenueSeatsUseCase.execute(venueId));
    }

    @PostMapping("/venues/{venueId}/seats")
    @Override
    public ResponseEntity<CreatedCountResponse> createSeats(
            @PathVariable Long venueId,
            @Valid @RequestBody RegisterVenueSeatsRequest request
    ) {
        return ResponseEntity.ok(registerVenueSeatsUseCase.execute(venueId, request));
    }

    @GetMapping("/venues/{venueId}/sections")
    @Override
    public ResponseEntity<VenueSectionListResponse> listSections(
            @PathVariable Long venueId
    ) {
        return ResponseEntity.ok(listVenueSectionsUseCase.execute(venueId));
    }

    @PostMapping("/venues/{venueId}/sections")
    @Override
    public ResponseEntity<CreatedCountResponse> createSections(
            @PathVariable Long venueId,
            @Valid @RequestBody RegisterVenueSectionsRequest request
    ) {
        return ResponseEntity.ok(registerVenueSectionsUseCase.execute(venueId, request));
    }

    @PostMapping("/events")
    @Override
    public ResponseEntity<CreateEventResponse> createEvent(
            Authentication authentication,
            @Valid @RequestBody CreateEventRequest request
    ) {
        return ResponseEntity.ok(createEventUseCase.execute(Long.valueOf(authentication.getName()), request));
    }

    @GetMapping("/events")
    @Override
    public ResponseEntity<AdminEventListResponse> listEvents(
            Authentication authentication
    ) {
        return ResponseEntity.ok(AdminEventListResponse.from(
                listAdminEventsUseCase.execute(Long.valueOf(authentication.getName()))
        ));
    }

    @PostMapping("/shows")
    @Override
    public ResponseEntity<CreateShowResponse> createShow(
            Authentication authentication,
            @Valid @RequestBody CreateShowRequest request
    ) {
        return ResponseEntity.ok(createShowUseCase.execute(Long.valueOf(authentication.getName()), request));
    }

    @GetMapping("/shows")
    @Override
    public ResponseEntity<AdminShowListResponse> listShows(
            Authentication authentication
    ) {
        return ResponseEntity.ok(AdminShowListResponse.from(
                listAdminShowsUseCase.execute(Long.valueOf(authentication.getName()))
        ));
    }

    @PostMapping("/shows/{showId}/show-seats")
    @Override
    public ResponseEntity<CreatedCountResponse> createShowSeats(
            @PathVariable Long showId,
            @Valid @RequestBody CreateShowSeatsRequest request
    ) {
        return ResponseEntity.ok(createShowSeatsUseCase.execute(showId, request));
    }

    @PostMapping("/shows/{showId}/section-inventories")
    @Override
    public ResponseEntity<CreatedCountResponse> createShowSectionInventories(
            @PathVariable Long showId,
            @Valid @RequestBody CreateShowSectionInventoriesRequest request
    ) {
        return ResponseEntity.ok(createShowSectionInventoriesUseCase.execute(showId, request));
    }
}
