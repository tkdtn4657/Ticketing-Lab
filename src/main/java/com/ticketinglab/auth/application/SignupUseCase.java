package com.ticketinglab.auth.application;

import com.ticketinglab.auth.presentation.dto.SignupCreateDto;
import com.ticketinglab.auth.presentation.dto.SignupRequest;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignupUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String execute(SignupRequest req){
        if(userRepository.existsByEmail(req.email())){
            throw new IllegalArgumentException("");
        }

        String passwordHash = passwordEncoder.encode(req.password());

        User user = User.createUser(new SignupCreateDto(req.email(), passwordHash));

        userRepository.save(user);

        return "OK";
    }

}
