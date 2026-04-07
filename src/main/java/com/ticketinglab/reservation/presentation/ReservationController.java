package com.ticketinglab.reservation.presentation;

import com.ticketinglab.config.openapi.OpenApiExamples;
import com.ticketinglab.reservation.application.CreateReservationUseCase;
import com.ticketinglab.reservation.application.GetReservationUseCase;
import com.ticketinglab.reservation.application.ListMyReservationsUseCase;
import com.ticketinglab.reservation.presentation.dto.CreateReservationRequest;
import com.ticketinglab.reservation.presentation.dto.CreateReservationResponse;
import com.ticketinglab.reservation.presentation.dto.MyReservationListResponse;
import com.ticketinglab.reservation.presentation.dto.ReservationDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Reservation")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final GetReservationUseCase getReservationUseCase;
    private final ListMyReservationsUseCase listMyReservationsUseCase;

    @Operation(summary = "예약 생성", description = "RES-001. 활성 홀드를 결제 대기 상태의 예약으로 전환합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "예약 생성 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateReservationRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.RESERVATION_CREATE_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 생성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateReservationResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.RESERVATION_CREATE_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "홀드를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "전환할 수 없는 홀드 상태입니다.")
    })
    @PostMapping("/api/reservations")
    public ResponseEntity<CreateReservationResponse> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        CreateReservationResponse response = createReservationUseCase.execute(
                Long.valueOf(authentication.getName()),
                request
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "예약 상세 조회", description = "RES-002. 본인 예약의 금액, 만료 시각, 예약 아이템 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 상세 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReservationDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.RESERVATION_DETAIL_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없습니다.")
    })
    @GetMapping("/api/reservations/{reservationId}")
    public ResponseEntity<ReservationDetailResponse> detail(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "예약 ID", example = "c8a01d2c-592a-43d6-9afb-a0df27c0c1c4")
            @PathVariable String reservationId
    ) {
        ReservationDetailResponse response = getReservationUseCase.execute(
                Long.valueOf(authentication.getName()),
                reservationId
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 예약 목록 조회", description = "RES-003. 페이지네이션과 상태 필터로 현재 사용자의 예약 목록을 조회합니다. 조회 시 만료 예약을 정리합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 예약 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MyReservationListResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.RESERVATION_LIST_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "페이지 정보 또는 상태 필터가 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다.")
    })
    @GetMapping("/api/me/reservations")
    public ResponseEntity<MyReservationListResponse> listMine(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(
                    description = "예약 상태 필터",
                    schema = @Schema(allowableValues = {"PENDING_PAYMENT", "PAID", "CANCELED", "EXPIRED"}),
                    example = "EXPIRED"
            )
            @RequestParam(required = false) String status
    ) {
        MyReservationListResponse response = listMyReservationsUseCase.execute(
                Long.valueOf(authentication.getName()),
                page,
                size,
                status
        );
        return ResponseEntity.ok(response);
    }
}