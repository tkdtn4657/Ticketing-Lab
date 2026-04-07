package com.ticketinglab.auth.presentation;

import com.ticketinglab.auth.application.GetMyInfoUseCase;
import com.ticketinglab.auth.application.LoginUseCase;
import com.ticketinglab.auth.application.LogoutUseCase;
import com.ticketinglab.auth.application.RefreshTokenUseCase;
import com.ticketinglab.auth.application.SignupUseCase;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
    private static final long REFRESH_TOKEN_MAX_AGE_SECONDS = 14L * 24 * 3600;

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetMyInfoUseCase getMyInfoUseCase;

    @Operation(summary = "회원가입", description = "AUTH-001. 이메일과 비밀번호로 USER 계정을 생성합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        SignupResponse response = signupUseCase.execute(req);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "AUTH-002. 로그인 성공 시 Access Token과 Refresh Token을 함께 반환합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/login")
    public ResponseEntity<TokenPair> login(@Valid @RequestBody LoginRequest req) {
        TokenPair tokens = loginUseCase.execute(req);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokens.accessToken())
                .header("Set-Cookie", createRefreshCookie(tokens.refreshToken()).toString())
                .body(tokens);
    }

    @Operation(summary = "토큰 재발급", description = "AUTH-003. Refresh Token을 검증하고 Access Token과 Refresh Token을 함께 재발급합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/refresh")
    public ResponseEntity<TokenPair> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        TokenPair tokens = refreshTokenUseCase.execute(req.refreshToken());

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokens.accessToken())
                .header("Set-Cookie", createRefreshCookie(tokens.refreshToken()).toString())
                .body(tokens);
    }

    @Operation(summary = "로그아웃", description = "AUTH-004. 현재 사용자 기준으로 Refresh Token을 무효화하고 쿠키를 삭제합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody RefreshTokenRequest req
    ) {
        logoutUseCase.execute(Long.valueOf(authentication.getName()), req.refreshToken());

        return ResponseEntity.noContent()
                .header("Set-Cookie", clearRefreshCookie().toString())
                .build();
    }

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
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@Parameter(hidden = true) Authentication authentication) {
        CurrentUserResponse response = getMyInfoUseCase.execute(Long.valueOf(authentication.getName()));

        return ResponseEntity.ok(response);
    }

    private ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE_SECONDS)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }
}