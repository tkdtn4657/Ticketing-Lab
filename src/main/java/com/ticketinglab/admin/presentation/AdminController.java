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
import com.ticketinglab.config.openapi.OpenApiExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@Tag(name = "Admin")
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

    @Operation(summary = "공연장 등록/수정", description = "ADM-001. 공연장 코드를 기준으로 공연장을 생성하거나 수정합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/venues/upsert")
    public ResponseEntity<VenueUpsertResponse> upsertVenue(@Valid @RequestBody VenueUpsertRequest request) {
        return ResponseEntity.ok(upsertVenueUseCase.execute(request));
    }

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
    @GetMapping("/venues/{venueId}/seats")
    public ResponseEntity<VenueSeatListResponse> listSeats(
            @Parameter(description = "공연장 ID", example = "1")
            @PathVariable Long venueId
    ) {
        return ResponseEntity.ok(listVenueSeatsUseCase.execute(venueId));
    }

    @Operation(summary = "공연장 좌석 기준정보 등록", description = "ADM-002. 공연장 기준 좌석 마스터를 일괄 등록합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/venues/{venueId}/seats")
    public ResponseEntity<CreatedCountResponse> createSeats(
            @Parameter(description = "공연장 ID", example = "1")
            @PathVariable Long venueId,
            @Valid @RequestBody RegisterVenueSeatsRequest request
    ) {
        return ResponseEntity.ok(registerVenueSeatsUseCase.execute(venueId, request));
    }

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
    @GetMapping("/venues/{venueId}/sections")
    public ResponseEntity<VenueSectionListResponse> listSections(
            @Parameter(description = "공연장 ID", example = "1")
            @PathVariable Long venueId
    ) {
        return ResponseEntity.ok(listVenueSectionsUseCase.execute(venueId));
    }

    @Operation(summary = "공연장 구역 기준정보 등록", description = "ADM-003. 공연장 기준 구역 마스터를 일괄 등록합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/venues/{venueId}/sections")
    public ResponseEntity<CreatedCountResponse> createSections(
            @Parameter(description = "공연장 ID", example = "1")
            @PathVariable Long venueId,
            @Valid @RequestBody RegisterVenueSectionsRequest request
    ) {
        return ResponseEntity.ok(registerVenueSectionsUseCase.execute(venueId, request));
    }

    @Operation(summary = "이벤트 생성", description = "ADM-004. 이벤트 제목, 설명, 상태를 받아 이벤트를 생성합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/events")
    public ResponseEntity<CreateEventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.ok(createEventUseCase.execute(request));
    }

    @Operation(summary = "회차 생성", description = "ADM-005. 이벤트와 공연장을 연결해 회차를 생성합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/shows")
    public ResponseEntity<CreateShowResponse> createShow(@Valid @RequestBody CreateShowRequest request) {
        return ResponseEntity.ok(createShowUseCase.execute(request));
    }

    @Operation(summary = "회차 좌석 판매 정보 생성", description = "ADM-006. 회차별 판매 좌석과 가격 정보를 일괄 생성합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/shows/{showId}/show-seats")
    public ResponseEntity<CreatedCountResponse> createShowSeats(
            @Parameter(description = "회차 ID", example = "200")
            @PathVariable Long showId,
            @Valid @RequestBody CreateShowSeatsRequest request
    ) {
        return ResponseEntity.ok(createShowSeatsUseCase.execute(showId, request));
    }

    @Operation(summary = "회차 구역 재고 생성", description = "ADM-007. 회차별 구역 가격과 수량 정보를 일괄 생성합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/shows/{showId}/section-inventories")
    public ResponseEntity<CreatedCountResponse> createShowSectionInventories(
            @Parameter(description = "회차 ID", example = "200")
            @PathVariable Long showId,
            @Valid @RequestBody CreateShowSectionInventoriesRequest request
    ) {
        return ResponseEntity.ok(createShowSectionInventoriesUseCase.execute(showId, request));
    }
}