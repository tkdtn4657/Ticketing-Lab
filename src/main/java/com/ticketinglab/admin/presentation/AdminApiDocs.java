package com.ticketinglab.admin.presentation;

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
import com.ticketinglab.config.openapi.OpenApiExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Admin")
public interface AdminApiDocs {

    @Operation(summary = "공연장 등록/수정", description = "ADM-001. 공연장 코드를 기준으로 공연장을 생성하거나 수정합니다.")
    @RequestBody(
            required = true,
            description = "공연장 upsert 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = VenueUpsertRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.ADMIN_VENUE_UPSERT_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공연장 저장 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VenueUpsertResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_VENUE_UPSERT_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "공연장 요청 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다.")
    })
    ResponseEntity<VenueUpsertResponse> upsertVenue(
            @Parameter(hidden = true) Authentication authentication,
            @Valid VenueUpsertRequest request
    );

    @Operation(summary = "내 공연장 목록 조회", description = "ADM-010. 현재 관리자가 생성한 공연장 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공연장 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminVenueListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다.")
    })
    ResponseEntity<AdminVenueListResponse> listVenues(
            @Parameter(hidden = true) Authentication authentication
    );

    @Operation(summary = "공연장 좌석 기준정보 조회", description = "ADM-008. 공연장에 등록된 좌석 마스터 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "좌석 기준정보 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VenueSeatListResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_VENUE_SEATS_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "공연장을 찾을 수 없습니다.")
    })
    ResponseEntity<VenueSeatListResponse> listSeats(
            @Parameter(description = "공연장 ID", example = "1")
            Long venueId
    );

    @Operation(summary = "공연장 좌석 기준정보 등록", description = "ADM-002. 공연장 기준 좌석 마스터를 일괄 등록합니다.")
    @RequestBody(
            required = true,
            description = "좌석 기준정보 등록 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterVenueSeatsRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.ADMIN_REGISTER_SEATS_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "좌석 기준정보 등록 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreatedCountResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATED_COUNT_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "좌석 요청 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "공연장을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "중복 좌석 라벨이 있거나 이미 존재하는 좌석입니다.")
    })
    ResponseEntity<CreatedCountResponse> createSeats(
            @Parameter(description = "공연장 ID", example = "1")
            Long venueId,
            @Valid RegisterVenueSeatsRequest request
    );

    @Operation(summary = "공연장 구역 기준정보 조회", description = "ADM-009. 공연장에 등록된 구역 마스터 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구역 기준정보 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VenueSectionListResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_VENUE_SECTIONS_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "공연장을 찾을 수 없습니다.")
    })
    ResponseEntity<VenueSectionListResponse> listSections(
            @Parameter(description = "공연장 ID", example = "1")
            Long venueId
    );

    @Operation(summary = "공연장 구역 기준정보 등록", description = "ADM-003. 공연장 기준 구역 마스터를 일괄 등록합니다.")
    @RequestBody(
            required = true,
            description = "구역 기준정보 등록 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterVenueSectionsRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.ADMIN_REGISTER_SECTIONS_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구역 기준정보 등록 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreatedCountResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATED_COUNT_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "구역 요청 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "공연장을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "중복 구역명이 있거나 이미 존재하는 구역입니다.")
    })
    ResponseEntity<CreatedCountResponse> createSections(
            @Parameter(description = "공연장 ID", example = "1")
            Long venueId,
            @Valid RegisterVenueSectionsRequest request
    );

    @Operation(summary = "이벤트 생성", description = "ADM-004. 이벤트 제목, 설명, 상태를 받아 이벤트를 생성합니다.")
    @RequestBody(
            required = true,
            description = "이벤트 생성 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateEventRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATE_EVENT_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateEventResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATE_EVENT_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 이벤트 상태이거나 요청 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다.")
    })
    ResponseEntity<CreateEventResponse> createEvent(
            @Parameter(hidden = true) Authentication authentication,
            @Valid CreateEventRequest request
    );

    @Operation(summary = "내 이벤트 목록 조회", description = "ADM-011. 현재 관리자가 생성한 이벤트 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminEventListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다.")
    })
    ResponseEntity<AdminEventListResponse> listEvents(
            @Parameter(hidden = true) Authentication authentication
    );

    @Operation(summary = "회차 생성", description = "ADM-005. 이벤트와 공연장을 연결해 회차를 생성합니다.")
    @RequestBody(
            required = true,
            description = "회차 생성 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateShowRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATE_SHOW_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회차 생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateShowResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATE_SHOW_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "이벤트 또는 공연장을 찾을 수 없습니다.")
    })
    ResponseEntity<CreateShowResponse> createShow(
            @Parameter(hidden = true) Authentication authentication,
            @Valid CreateShowRequest request
    );

    @Operation(summary = "내 회차 목록 조회", description = "ADM-012. 현재 관리자가 생성한 회차 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회차 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminShowListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다.")
    })
    ResponseEntity<AdminShowListResponse> listShows(
            @Parameter(hidden = true) Authentication authentication
    );

    @Operation(summary = "회차 좌석 판매 정보 생성", description = "ADM-006. 회차별 판매 좌석과 가격 정보를 일괄 생성합니다.")
    @RequestBody(
            required = true,
            description = "회차 좌석 판매 정보 생성 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateShowSeatsRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATE_SHOW_SEATS_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회차 좌석 판매 정보 생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreatedCountResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATED_COUNT_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "좌석 요청 형식이 올바르지 않거나 seatId가 잘못되었습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "회차를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "중복 seatId가 있거나 이미 생성된 회차 좌석입니다.")
    })
    ResponseEntity<CreatedCountResponse> createShowSeats(
            @Parameter(description = "회차 ID", example = "200")
            Long showId,
            @Valid CreateShowSeatsRequest request
    );

    @Operation(summary = "회차 구역 재고 생성", description = "ADM-007. 회차별 구역 가격과 수량 정보를 일괄 생성합니다.")
    @RequestBody(
            required = true,
            description = "회차 구역 재고 생성 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateShowSectionInventoriesRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATE_SECTION_INVENTORIES_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회차 구역 재고 생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreatedCountResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.ADMIN_CREATED_COUNT_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "구역 요청 형식이 올바르지 않거나 sectionId가 잘못되었습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "회차를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "중복 sectionId가 있거나 이미 생성된 회차 구역 재고입니다.")
    })
    ResponseEntity<CreatedCountResponse> createShowSectionInventories(
            @Parameter(description = "회차 ID", example = "200")
            Long showId,
            @Valid CreateShowSectionInventoriesRequest request
    );
}
