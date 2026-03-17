package com.ticketinglab.auth.application;

import com.ticketinglab.auth.presentation.dto.CurrentUserResponse;
import com.ticketinglab.user.domain.User;
import com.ticketinglab.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class GetMyInfoUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CurrentUserResponse execute(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found"));

        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getRole());
    }
}
