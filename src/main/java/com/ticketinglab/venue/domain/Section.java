package com.ticketinglab.venue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    public static Section create(String name, Long venueId) {
        return Section.builder()
                .name(name)
                .createdAt(LocalDateTime.now())
                .venueId(venueId)
                .build();
    }

    @Builder
    public Section(
            Long id,
            String name,
            LocalDateTime createdAt,
            Long venueId
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.venueId = venueId;
    }
}
