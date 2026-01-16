package com.ticketinglab.user.infrastructure.jpa;

import com.ticketinglab.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
