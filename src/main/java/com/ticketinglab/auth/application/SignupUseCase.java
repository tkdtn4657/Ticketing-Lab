package com.ticketinglab.auth.application;

import com.ticketinglab.auth.presentation.dto.SignupResponse;
import com.ticketinglab.auth.presentation.dto.SignupRequest;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(SignupRequest req){
        if(userRepository.existsByEmail(req.email())){
            throw new IllegalArgumentException("이미 가입 된 회원입니다.");
        }

        String passwordHash = passwordEncoder.encode(req.password());

        User user = User.createUser(
                new SignupResponse(
                        req.email(),
                        passwordHash)
        );

        userRepository.save(user);
    }

}
