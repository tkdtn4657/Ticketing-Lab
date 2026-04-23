package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.event.domain.Show;

import java.util.List;

public record AdminShowListResponse(List<AdminShowResponse> shows) {

    public static AdminShowListResponse from(List<Show> shows) {
        return new AdminShowListResponse(
                shows.stream()
                        .map(AdminShowResponse::from)
                        .toList()
        );
    }
}
