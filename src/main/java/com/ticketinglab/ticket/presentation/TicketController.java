package com.ticketinglab.ticket.presentation;

import com.ticketinglab.config.openapi.OpenApiExamples;
import com.ticketinglab.ticket.application.ListMyTicketsUseCase;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Ticket")
public class TicketController {

    private final ListMyTicketsUseCase listMyTicketsUseCase;

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
    @GetMapping("/api/me/tickets")
    public ResponseEntity<MyTicketListResponse> listMine(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        MyTicketListResponse response = listMyTicketsUseCase.execute(
                Long.valueOf(authentication.getName()),
                page,
                size
        );
        return ResponseEntity.ok(response);
    }
}