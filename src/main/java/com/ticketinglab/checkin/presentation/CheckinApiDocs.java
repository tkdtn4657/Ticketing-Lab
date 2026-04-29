package com.ticketinglab.checkin.presentation;

import com.ticketinglab.checkin.presentation.dto.CheckinRequest;
import com.ticketinglab.checkin.presentation.dto.CheckinResponse;
import com.ticketinglab.config.openapi.OpenApiExamples;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Checkin")
public interface CheckinApiDocs {

    @Operation(summary = "체크인 처리", description = "CHK-001. ADMIN 권한으로 qrToken을 검증하고 티켓을 USED 상태로 전환합니다.")
    @RequestBody(
            required = true,
            description = "체크인 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CheckinRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.CHECKIN_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "체크인 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckinResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.CHECKIN_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "해당 QR 토큰의 티켓을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용된 티켓입니다.")
    })
    ResponseEntity<CheckinResponse> checkin(@Valid CheckinRequest request);
}
