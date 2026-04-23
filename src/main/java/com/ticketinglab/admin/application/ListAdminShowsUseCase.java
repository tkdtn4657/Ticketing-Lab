package com.ticketinglab.admin.application;

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
    public List<Show> execute(Long userId) {
        return showRepository.findAllByCreatedByUserId(userId);
    }
}
