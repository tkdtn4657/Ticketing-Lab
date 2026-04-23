package com.ticketinglab.admin.presentation;

import com.ticketinglab.admin.application.ListMasterEventsUseCase;
import com.ticketinglab.admin.application.ListMasterShowsUseCase;
import com.ticketinglab.admin.application.ListMasterVenuesUseCase;
import com.ticketinglab.admin.presentation.dto.AdminEventListResponse;
import com.ticketinglab.admin.presentation.dto.AdminShowListResponse;
import com.ticketinglab.admin.presentation.dto.AdminVenueListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
@Tag(name = "Master Admin")
public class MasterAdminController {

    private final ListMasterVenuesUseCase listMasterVenuesUseCase;
    private final ListMasterEventsUseCase listMasterEventsUseCase;
    private final ListMasterShowsUseCase listMasterShowsUseCase;

    @Operation(summary = "전체 공연장 목록 조회", description = "MST-001. MASTER_ADMIN 전용 전체 공연장 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 공연장 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminVenueListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "MASTER_ADMIN 권한이 필요합니다.")
    })
    @GetMapping("/venues")
    public ResponseEntity<AdminVenueListResponse> listVenues() {
        return ResponseEntity.ok(AdminVenueListResponse.from(listMasterVenuesUseCase.execute()));
    }

    @Operation(summary = "전체 이벤트 목록 조회", description = "MST-002. MASTER_ADMIN 전용 전체 이벤트 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 이벤트 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminEventListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "MASTER_ADMIN 권한이 필요합니다.")
    })
    @GetMapping("/events")
    public ResponseEntity<AdminEventListResponse> listEvents() {
        return ResponseEntity.ok(AdminEventListResponse.from(listMasterEventsUseCase.execute()));
    }

    @Operation(summary = "전체 회차 목록 조회", description = "MST-003. MASTER_ADMIN 전용 전체 회차 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 회차 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminShowListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "MASTER_ADMIN 권한이 필요합니다.")
    })
    @GetMapping("/shows")
    public ResponseEntity<AdminShowListResponse> listShows() {
        return ResponseEntity.ok(AdminShowListResponse.from(listMasterShowsUseCase.execute()));
    }
}
