package com.ticketinglab.auth.application;

import com.ticketinglab.auth.infrastructure.jwt.JwtTokenProvider;
import com.ticketinglab.auth.presentation.dto.LoginRequest;
import com.ticketinglab.auth.presentation.dto.TokenPair;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenPair execute(LoginRequest req){
        User user = userRepository.findByEmail(req.email()).orElseThrow(
                () -> new IllegalArgumentException("login fail"));

        String passwordHash = user.getPasswordHash();
        if(!passwordEncoder.matches(req.password(), passwordHash)){
            throw new IllegalArgumentException("login fail");
        }

        return jwtTokenProvider.createTokens(
                user.getId(),
                user.getEmail(),
                user.getRole());
    }

}
