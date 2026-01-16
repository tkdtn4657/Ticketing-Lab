package com.ticketinglab.user.infrastructure;

import com.ticketinglab.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
