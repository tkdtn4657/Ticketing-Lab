package com.ticketinglab.checkin.presentation;

import com.ticketinglab.checkin.application.CheckinUseCase;
import com.ticketinglab.checkin.presentation.dto.CheckinRequest;
import com.ticketinglab.checkin.presentation.dto.CheckinResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class CheckinController implements CheckinApiDocs {

    private final CheckinUseCase checkinUseCase;

    @PostMapping("/api/checkin")
    @Override
    public ResponseEntity<CheckinResponse> checkin(@Valid @RequestBody CheckinRequest request) {
        return ResponseEntity.ok(checkinUseCase.execute(request.qrToken()));
    }
}
