package com.ticketinglab.auth.infrastructure;

import com.ticketinglab.auth.domain.RefreshToken;
import com.ticketinglab.auth.domain.RefreshTokenRepository;
import com.ticketinglab.auth.infrastructure.jpa.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return jpaRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findById(token);
    }

    @Override
    public void deleteByToken(String token) {
        jpaRepository.deleteById(token);
    }
}