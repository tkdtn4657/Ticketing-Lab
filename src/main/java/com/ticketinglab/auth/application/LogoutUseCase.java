package com.ticketinglab.auth.application;

import com.ticketinglab.auth.domain.TokenSession;
import com.ticketinglab.auth.domain.TokenSessionRepository;
import com.ticketinglab.auth.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenSessionRepository tokenSessionRepository;

    @Transactional
    public void execute(Long userId, String refreshToken) {
        if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid refresh token");
        }

        Long refreshTokenUserId = jwtTokenProvider.getUserId(refreshToken);
        if (!userId.equals(refreshTokenUserId)) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid refresh token");
        }

        TokenSession tokenSession = tokenSessionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "invalid refresh token"));
        if (!tokenSession.hasRefreshToken(refreshToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid refresh token");
        }

        tokenSessionRepository.deleteByUserId(userId);
    }
}
