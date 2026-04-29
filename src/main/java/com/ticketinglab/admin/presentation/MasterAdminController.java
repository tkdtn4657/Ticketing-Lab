package com.ticketinglab.admin.presentation;

import com.ticketinglab.admin.application.ListMasterEventsUseCase;
import com.ticketinglab.admin.application.ListMasterShowsUseCase;
import com.ticketinglab.admin.application.ListMasterVenuesUseCase;
import com.ticketinglab.admin.presentation.dto.AdminEventListResponse;
import com.ticketinglab.admin.presentation.dto.AdminShowListResponse;
import com.ticketinglab.admin.presentation.dto.AdminVenueListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterAdminController implements MasterAdminApiDocs {

    private final ListMasterVenuesUseCase listMasterVenuesUseCase;
    private final ListMasterEventsUseCase listMasterEventsUseCase;
    private final ListMasterShowsUseCase listMasterShowsUseCase;

    @GetMapping("/venues")
    @Override
    public ResponseEntity<AdminVenueListResponse> listVenues() {
        return ResponseEntity.ok(AdminVenueListResponse.from(listMasterVenuesUseCase.execute()));
    }

    @GetMapping("/events")
    @Override
    public ResponseEntity<AdminEventListResponse> listEvents() {
        return ResponseEntity.ok(AdminEventListResponse.from(listMasterEventsUseCase.execute()));
    }

    @GetMapping("/shows")
    @Override
    public ResponseEntity<AdminShowListResponse> listShows() {
        return ResponseEntity.ok(AdminShowListResponse.from(listMasterShowsUseCase.execute()));
    }
}
