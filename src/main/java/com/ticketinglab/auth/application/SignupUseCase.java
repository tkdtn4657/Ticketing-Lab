package com.ticketinglab.auth.application;

import com.ticketinglab.auth.presentation.dto.SignupRequest;
import com.ticketinglab.auth.presentation.dto.SignupResponse;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class SignupUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse execute(SignupRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(CONFLICT, "email already exists");
        }

        String passwordHash = passwordEncoder.encode(req.password());
        User user = User.createUser(req.email(), passwordHash);
        User savedUser = userRepository.save(user);

        return new SignupResponse(savedUser.getId());
    }
}