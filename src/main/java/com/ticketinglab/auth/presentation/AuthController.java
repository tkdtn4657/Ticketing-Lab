package com.ticketinglab.auth.presentation;

import com.ticketinglab.auth.application.LoginUseCase;
import com.ticketinglab.auth.application.SignupUseCase;
import com.ticketinglab.auth.presentation.dto.LoginRequest;
import com.ticketinglab.auth.presentation.dto.SignupRequest;
import com.ticketinglab.auth.presentation.dto.TokenPair;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest req){
        signupUseCase.execute(req);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest req){
        TokenPair tokens = loginUseCase.execute(req);
        ResponseCookie refreshCookie =
                ResponseCookie.from("refresh-token", tokens.refreshToken())
                        .httpOnly(true)
                        .secure(false)
                        .maxAge(14 * 24 * 3600)
                        .build();

        return ResponseEntity.ok()
                .header("Authorization",
                        "Bearer " + tokens.accessToken())
                .header("Set-Cookie", refreshCookie.toString())
                .build();
    }

}
