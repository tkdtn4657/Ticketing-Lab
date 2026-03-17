package com.ticketinglab.auth.application;

import com.ticketinglab.auth.domain.RefreshToken;
import com.ticketinglab.auth.domain.RefreshTokenRepository;
import com.ticketinglab.auth.infrastructure.jwt.JwtTokenProvider;
import com.ticketinglab.auth.presentation.dto.LoginRequest;
import com.ticketinglab.auth.presentation.dto.TokenPair;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenPair execute(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "login failed"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "login failed");
        }

        TokenPair tokens = jwtTokenProvider.createTokens(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        refreshTokenRepository.save(RefreshToken.issue(tokens.refreshToken(), user.getId()));
        return tokens;
    }
}