package com.ticketinglab.hold.application;

import com.ticketinglab.hold.domain.Hold;
import com.ticketinglab.hold.domain.HoldRepository;
import com.ticketinglab.hold.presentation.dto.HoldDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class GetHoldUseCase {

    private final HoldRepository holdRepository;
    private final HoldResourceManager holdResourceManager;

    @Transactional
    public HoldDetailResponse execute(Long userId, String holdId) {
        Hold hold = holdRepository.findById(holdId)
                .filter(found -> found.isOwnedBy(userId))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "hold not found"));

        holdResourceManager.expire(hold, LocalDateTime.now());
        return HoldDetailResponse.from(hold);
    }
}
