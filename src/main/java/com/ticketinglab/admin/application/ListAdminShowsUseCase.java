package com.ticketinglab.admin.application;

import com.ticketinglab.admin.presentation.dto.AdminShowListResponse;
import com.ticketinglab.admin.presentation.dto.AdminShowResponse;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAdminShowsUseCase {

    private final ShowRepository showRepository;

    @Transactional(readOnly = true)
    public AdminShowListResponse execute(Long userId) {
        List<Show> shows = showRepository.findAllByCreatedByUserId(userId);

        return new AdminShowListResponse(
                shows.stream()
                        .map(AdminShowResponse::from)
                        .toList()
        );
    }
}
