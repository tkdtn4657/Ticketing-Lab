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
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "label", nullable = false, length = 50)
    private String label;

    @Column(name = "row_no")
    private Integer rowNo;

    @Column(name = "col_no")
    private Integer colNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    public static Seat create(String label, Integer rowNo, Integer colNo, Long venueId) {
        return Seat.builder()
                .label(label)
                .rowNo(rowNo)
                .colNo(colNo)
                .createdAt(LocalDateTime.now())
                .venueId(venueId)
                .build();
    }

    @Builder
    public Seat(
            Long id,
            String label,
            Integer rowNo,
            Integer colNo,
            LocalDateTime createdAt,
            Long venueId
    ) {
        this.id = id;
        this.label = label;
        this.rowNo = rowNo;
        this.colNo = colNo;
        this.createdAt = createdAt;
        this.venueId = venueId;
    }
}
