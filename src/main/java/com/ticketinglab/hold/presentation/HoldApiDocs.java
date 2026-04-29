package com.ticketinglab.hold.presentation;

import com.ticketinglab.config.openapi.OpenApiExamples;
import com.ticketinglab.hold.presentation.dto.CreateHoldRequest;
import com.ticketinglab.hold.presentation.dto.CreateHoldResponse;
import com.ticketinglab.hold.presentation.dto.HoldDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Hold")
public interface HoldApiDocs {

    @Operation(summary = "홀드 생성", description = "HLD-001. 회차 기준으로 좌석/구역을 임시 선점합니다. 좌석은 qty=1, 구역은 qty를 필수로 전달합니다.")
    @RequestBody(
            required = true,
            description = "홀드 생성 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateHoldRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.HOLD_CREATE_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "홀드 생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateHoldResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.HOLD_CREATE_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "홀드 요청 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "회차를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "이미 점유된 좌석/구역이 포함되어 있습니다.")
    })
    ResponseEntity<CreateHoldResponse> create(
            @Parameter(hidden = true) Authentication authentication,
            CreateHoldRequest request
    );

    @Operation(summary = "홀드 상세 조회", description = "HLD-002. 본인 홀드의 상태와 아이템 목록을 조회합니다. 만료된 홀드는 조회 시 EXPIRED로 정리됩니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "홀드 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = HoldDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.HOLD_DETAIL_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "홀드를 찾을 수 없습니다.")
    })
    ResponseEntity<HoldDetailResponse> detail(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "홀드 ID", example = "06f0cd71-93bf-40b6-850e-e5593f8e7a44")
            String holdId
    );

    @Operation(summary = "홀드 취소", description = "HLD-003. 본인 홀드를 취소하고 점유 중인 자원을 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "홀드 취소 성공"),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "홀드를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "이미 전환되었거나 취소할 수 없는 홀드입니다.")
    })
    ResponseEntity<Void> cancel(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "홀드 ID", example = "06f0cd71-93bf-40b6-850e-e5593f8e7a44")
            String holdId
    );
}
