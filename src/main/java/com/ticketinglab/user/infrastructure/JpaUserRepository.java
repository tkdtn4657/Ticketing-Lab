package com.ticketinglab.user.infrastructure;

import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import com.ticketinglab.user.infrastructure.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

}
