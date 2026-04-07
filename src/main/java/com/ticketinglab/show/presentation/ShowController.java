package com.ticketinglab.show.presentation;

import com.ticketinglab.config.openapi.OpenApiExamples;
import com.ticketinglab.show.application.GetShowAvailabilityUseCase;
import com.ticketinglab.show.presentation.dto.ShowAvailabilityResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Tag(name = "Show")
public class ShowController {

    private final GetShowAvailabilityUseCase getShowAvailabilityUseCase;

    @Operation(summary = "회차 가용성 조회", description = "SHW-001. 회차별 좌석과 구역 재고 상태를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "가용성 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ShowAvailabilityResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.SHOW_AVAILABILITY_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "회차를 찾을 수 없습니다.")
    })
    @SecurityRequirements
    @GetMapping("/{showId}/availability")
    public ResponseEntity<ShowAvailabilityResponse> availability(
            @Parameter(description = "회차 ID", example = "101")
            @PathVariable Long showId
    ) {
        return ResponseEntity.ok(getShowAvailabilityUseCase.execute(showId));
    }
}