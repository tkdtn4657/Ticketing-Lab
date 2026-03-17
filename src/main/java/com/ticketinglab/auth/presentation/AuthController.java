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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
    private static final long REFRESH_TOKEN_MAX_AGE_SECONDS = 14L * 24 * 3600;

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetMyInfoUseCase getMyInfoUseCase;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        SignupResponse response = signupUseCase.execute(req);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPair> login(@Valid @RequestBody LoginRequest req) {
        TokenPair tokens = loginUseCase.execute(req);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokens.accessToken())
                .header("Set-Cookie", createRefreshCookie(tokens.refreshToken()).toString())
                .body(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPair> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        TokenPair tokens = refreshTokenUseCase.execute(req.refreshToken());

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokens.accessToken())
                .header("Set-Cookie", createRefreshCookie(tokens.refreshToken()).toString())
                .body(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            @Valid @RequestBody RefreshTokenRequest req
    ) {
        logoutUseCase.execute(Long.valueOf(authentication.getName()), req.refreshToken());

        return ResponseEntity.noContent()
                .header("Set-Cookie", clearRefreshCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(Authentication authentication) {
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