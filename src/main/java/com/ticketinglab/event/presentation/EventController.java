package com.ticketinglab.event.presentation;

import com.ticketinglab.config.openapi.OpenApiExamples;
import com.ticketinglab.event.application.GetEventDetailUseCase;
import com.ticketinglab.event.application.ListEventsUseCase;
import com.ticketinglab.event.presentation.dto.EventDetailResponse;
import com.ticketinglab.event.presentation.dto.EventListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event")
public class EventController {

    private final ListEventsUseCase listEventsUseCase;
    private final GetEventDetailUseCase getEventDetailUseCase;

    @Operation(summary = "이벤트 목록 조회", description = "EVT-001. 상태 필터를 기준으로 이벤트 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EventListResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.EVENT_LIST_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 이벤트 상태입니다.")
    })
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<EventListResponse> list(
            @Parameter(
                    description = "이벤트 상태 필터",
                    schema = @Schema(allowableValues = {"DRAFT", "PUBLISHED"}),
                    example = "PUBLISHED"
            )
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(listEventsUseCase.execute(status));
    }

    @Operation(summary = "이벤트 상세 조회", description = "EVT-002. 이벤트 기본 정보와 회차 목록을 함께 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이벤트 상세 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EventDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.EVENT_DETAIL_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없습니다.")
    })
    @SecurityRequirements
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponse> detail(
            @Parameter(description = "이벤트 ID", example = "11")
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(getEventDetailUseCase.execute(eventId));
    }
}