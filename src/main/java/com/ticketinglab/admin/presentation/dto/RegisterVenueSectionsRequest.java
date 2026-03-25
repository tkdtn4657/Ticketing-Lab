package com.ticketinglab.admin.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RegisterVenueSectionsRequest(
        @NotEmpty List<@Valid SectionItem> sections
) {
    public record SectionItem(
            @NotBlank String name
    ) {
    }
}