package com.ticketinglab.auth.infrastructure.jpa;

import com.ticketinglab.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, String> {
}