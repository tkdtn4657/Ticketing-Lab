package com.ticketinglab.auth.presentation;

import com.ticketinglab.auth.presentation.dto.CurrentUserResponse;
import com.ticketinglab.auth.presentation.dto.LoginRequest;
import com.ticketinglab.auth.presentation.dto.RefreshTokenRequest;
import com.ticketinglab.auth.presentation.dto.SignupRequest;
import com.ticketinglab.auth.presentation.dto.SignupResponse;
import com.ticketinglab.auth.presentation.dto.TokenPair;
import com.ticketinglab.config.openapi.OpenApiExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Auth")
public interface AuthApiDocs {

    @Operation(summary = "회원가입", description = "AUTH-001. 이메일과 비밀번호로 USER 계정을 생성합니다.")
    @RequestBody(
            required = true,
            description = "회원가입 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SignupRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.AUTH_SIGNUP_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignupResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.AUTH_SIGNUP_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "409", description = "이미 가입된 이메일입니다."),
            @ApiResponse(responseCode = "400", description = "이메일 또는 비밀번호를 확인해주세요.")
    })
    @SecurityRequirements
    ResponseEntity<SignupResponse> signup(@Valid SignupRequest req);

    @Operation(summary = "로그인", description = "AUTH-002. 로그인 성공 시 Access Token과 Refresh Token을 함께 반환합니다.")
    @RequestBody(
            required = true,
            description = "로그인 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.AUTH_LOGIN_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    headers = {
                            @Header(
                                    name = "Authorization",
                                    description = "Bearer Access Token",
                                    schema = @Schema(type = "string", example = OpenApiExamples.AUTHORIZATION_HEADER)
                            ),
                            @Header(
                                    name = "Set-Cookie",
                                    description = "HttpOnly Refresh Token Cookie",
                                    schema = @Schema(type = "string", example = OpenApiExamples.REFRESH_TOKEN_COOKIE)
                            )
                    },
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenPair.class),
                            examples = @ExampleObject(value = OpenApiExamples.AUTH_TOKEN_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않습니다."),
            @ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않습니다.")
    })
    @SecurityRequirements
    ResponseEntity<TokenPair> login(@Valid LoginRequest req);

    @Operation(summary = "토큰 재발급", description = "AUTH-003. Refresh Token을 검증하고 Access Token과 Refresh Token을 함께 재발급합니다.")
    @RequestBody(
            required = true,
            description = "토큰 재발급 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RefreshTokenRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.AUTH_REFRESH_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공",
                    headers = {
                            @Header(
                                    name = "Authorization",
                                    description = "Bearer Access Token",
                                    schema = @Schema(type = "string", example = OpenApiExamples.AUTHORIZATION_HEADER)
                            ),
                            @Header(
                                    name = "Set-Cookie",
                                    description = "회전된 Refresh Token Cookie",
                                    schema = @Schema(type = "string", example = OpenApiExamples.REFRESH_TOKEN_COOKIE)
                            )
                    },
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenPair.class),
                            examples = @ExampleObject(value = OpenApiExamples.AUTH_TOKEN_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Refresh Token이 유효하지 않습니다."),
            @ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않습니다.")
    })
    @SecurityRequirements
    ResponseEntity<TokenPair> refresh(@Valid RefreshTokenRequest req);

    @Operation(summary = "로그아웃", description = "AUTH-004. 현재 사용자 기준으로 Refresh Token을 무효화하고 쿠키를 삭제합니다.")
    @RequestBody(
            required = true,
            description = "로그아웃 요청",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RefreshTokenRequest.class),
                    examples = @ExampleObject(value = OpenApiExamples.AUTH_REFRESH_REQUEST)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "로그아웃 성공",
                    headers = @Header(
                            name = "Set-Cookie",
                            description = "삭제된 Refresh Token Cookie",
                            schema = @Schema(type = "string", example = OpenApiExamples.CLEARED_REFRESH_TOKEN_COOKIE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 정보 또는 Refresh Token이 유효하지 않습니다."),
            @ApiResponse(responseCode = "400", description = "요청 형식이 올바르지 않습니다.")
    })
    ResponseEntity<Void> logout(
            @Parameter(hidden = true) Authentication authentication,
            @Valid RefreshTokenRequest req
    );

    @Operation(summary = "내 정보 조회", description = "AUTH-005. 현재 인증된 사용자의 기본 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 정보 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CurrentUserResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.AUTH_ME_RESPONSE)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Bearer 토큰이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    ResponseEntity<CurrentUserResponse> me(@Parameter(hidden = true) Authentication authentication);
}
