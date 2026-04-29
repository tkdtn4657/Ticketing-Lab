package com.ticketinglab.auth.application;

import com.ticketinglab.auth.domain.TokenSession;
import com.ticketinglab.auth.domain.TokenSessionRepository;
import com.ticketinglab.auth.infrastructure.jwt.JwtTokenProvider;
import com.ticketinglab.auth.presentation.dto.TokenPair;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenSessionRepository tokenSessionRepository;

    @Transactional
    public TokenPair execute(String refreshToken) {
        if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid refresh token");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        TokenSession tokenSession = tokenSessionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "invalid refresh token"));
        if (!tokenSession.hasRefreshToken(refreshToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid refresh token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "invalid refresh token"));

        TokenPair newTokens = jwtTokenProvider.createTokens(user.getId(), user.getEmail(), user.getRole());
        tokenSessionRepository.save(
                TokenSession.issue(user.getId(), newTokens.accessToken(), newTokens.refreshToken()),
                jwtTokenProvider.getRefreshTokenTtl()
        );

        return newTokens;
    }
}
