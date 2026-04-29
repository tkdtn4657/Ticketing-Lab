package com.ticketinglab.hold.presentation;

import com.ticketinglab.hold.application.CancelHoldUseCase;
import com.ticketinglab.hold.application.CreateHoldUseCase;
import com.ticketinglab.hold.application.GetHoldUseCase;
import com.ticketinglab.hold.presentation.dto.CreateHoldRequest;
import com.ticketinglab.hold.presentation.dto.CreateHoldResponse;
import com.ticketinglab.hold.presentation.dto.HoldDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holds")
@RequiredArgsConstructor
public class HoldController implements HoldApiDocs {

    private final CreateHoldUseCase createHoldUseCase;
    private final GetHoldUseCase getHoldUseCase;
    private final CancelHoldUseCase cancelHoldUseCase;

    @PostMapping
    @Override
    public ResponseEntity<CreateHoldResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateHoldRequest request
    ) {
        CreateHoldResponse response = createHoldUseCase.execute(Long.valueOf(authentication.getName()), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{holdId}")
    @Override
    public ResponseEntity<HoldDetailResponse> detail(
            Authentication authentication,
            @PathVariable String holdId
    ) {
        HoldDetailResponse response = getHoldUseCase.execute(Long.valueOf(authentication.getName()), holdId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{holdId}")
    @Override
    public ResponseEntity<Void> cancel(
            Authentication authentication,
            @PathVariable String holdId
    ) {
        cancelHoldUseCase.execute(Long.valueOf(authentication.getName()), holdId);
        return ResponseEntity.noContent().build();
    }
}
