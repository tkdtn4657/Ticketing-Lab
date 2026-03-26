package com.ticketinglab.hold.application;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CancelHoldUseCase {

    private final HoldRepository holdRepository;
    private final HoldResourceManager holdResourceManager;

    @Transactional
    public void execute(Long userId, String holdId) {
        Hold hold = holdRepository.findById(holdId)
                .filter(found -> found.isOwnedBy(userId))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "hold not found"));

        holdResourceManager.expire(hold, LocalDateTime.now());

        try {
            holdResourceManager.cancel(hold);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }
    }
}
