package com.ticketinglab.admin.presentation;

import com.ticketinglab.admin.presentation.dto.AdminEventListResponse;
import com.ticketinglab.admin.presentation.dto.AdminShowListResponse;
import com.ticketinglab.admin.presentation.dto.AdminVenueListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Master Admin")
public interface MasterAdminApiDocs {

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
    ResponseEntity<AdminVenueListResponse> listVenues();

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
    ResponseEntity<AdminEventListResponse> listEvents();

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
    ResponseEntity<AdminShowListResponse> listShows();
}
