package com.ticketinglab.auth.presentation;

import com.ticketinglab.auth.application.SignupUseCase;
import com.ticketinglab.auth.presentation.dto.SignupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupUseCase signupUseCase;

    @PostMapping("/signup")
    public ResponseEntity<SignupRequest> signup(@Valid @RequestBody SignupRequest requestBody){

        log.info("requestData = {}", requestBody.email());

        signupUseCase.execute(requestBody);

        return ResponseEntity.ok(requestBody);
    }

}
