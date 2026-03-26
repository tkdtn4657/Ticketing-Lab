package com.ticketinglab.admin.presentation.dto;

import com.ticketinglab.venue.domain.Section;

public record VenueSectionResponse(
        Long sectionId,
        String name
) {
    public static VenueSectionResponse from(Section section) {
        return new VenueSectionResponse(
                section.getId(),
                section.getName()
        );
    }
}