package com.ticketinglab.ticket.presentation;

import com.ticketinglab.config.openapi.OpenApiExamples;
import com.ticketinglab.ticket.presentation.dto.MyTicketListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Ticket")
public interface TicketApiDocs {

    @Operation(summary = "내 티켓 목록 조회", description = "TKT-001. 결제 완료 후 발급된 티켓 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 티켓 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MyTicketListResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.TICKET_LIST_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "페이지 정보가 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다.")
    })
    ResponseEntity<MyTicketListResponse> listMine(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "페이지 번호", example = "0")
            @Min(0) int page,
            @Parameter(description = "페이지 크기", example = "20")
            @Min(1) @Max(100) int size
    );
}
