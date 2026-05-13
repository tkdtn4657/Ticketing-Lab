package com.ticketinglab.venue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "sections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sections_venue_name",
                columnNames = {"venue_id", "name"}
        )
)
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = false, length = 30, columnDefinition = "varchar(30) default 'GENERAL_ADMISSION'")
    private SectionSaleType saleType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    public static Section create(String name, Long venueId) {
        return create(name, SectionSaleType.GENERAL_ADMISSION, venueId);
    }

    public static Section create(String name, SectionSaleType saleType, Long venueId) {
        return Section.builder()
                .name(name)
                .saleType(saleType)
                .createdAt(LocalDateTime.now())
                .venueId(venueId)
                .build();
    }

    public boolean isAssignedSeatType() {
        return saleType == SectionSaleType.ASSIGNED_SEAT;
    }

    public boolean isGeneralAdmissionType() {
        return saleType == SectionSaleType.GENERAL_ADMISSION;
    }

    @Builder
    public Section(
            Long id,
            String name,
            SectionSaleType saleType,
            LocalDateTime createdAt,
            Long venueId
    ) {
        this.id = id;
        this.name = name;
        this.saleType = saleType == null ? SectionSaleType.GENERAL_ADMISSION : saleType;
        this.createdAt = createdAt;
        this.venueId = venueId;
    }
}
