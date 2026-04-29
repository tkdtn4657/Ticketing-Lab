package com.ticketinglab.show.presentation;

import com.ticketinglab.show.application.GetShowAvailabilityUseCase;
import com.ticketinglab.show.presentation.dto.ShowAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController implements ShowApiDocs {

    private final GetShowAvailabilityUseCase getShowAvailabilityUseCase;

    @GetMapping("/{showId}/availability")
    @Override
    public ResponseEntity<ShowAvailabilityResponse> availability(
            @PathVariable Long showId
    ) {
        return ResponseEntity.ok(getShowAvailabilityUseCase.execute(showId));
    }
}
