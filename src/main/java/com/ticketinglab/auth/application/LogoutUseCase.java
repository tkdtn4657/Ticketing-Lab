package com.ticketinglab.auth.application;

import com.ticketinglab.auth.domain.RefreshToken;
import com.ticketinglab.auth.domain.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(Long userId, String refreshToken) {
        if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid refresh token");
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "invalid refresh token"));

        Long refreshTokenUserId = jwtTokenProvider.getUserId(refreshToken);
        if (!userId.equals(refreshTokenUserId) || !userId.equals(savedRefreshToken.getUserId())) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid refresh token");
        }

        refreshTokenRepository.deleteByToken(refreshToken);
    }
}