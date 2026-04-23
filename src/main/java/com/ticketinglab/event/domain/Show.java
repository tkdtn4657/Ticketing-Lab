package com.ticketinglab.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShowStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    public static Show schedule(Event event, LocalDateTime startAt, Long venueId) {
        return schedule(event, startAt, venueId, null);
    }

    public static Show schedule(Event event, LocalDateTime startAt, Long venueId, Long createdByUserId) {
        return Show.builder()
                .event(event)
                .startAt(startAt)
                .status(ShowStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .venueId(venueId)
                .createdByUserId(createdByUserId)
                .build();
    }

    public static Show create(Event event, LocalDateTime startAt, ShowStatus status, Long venueId) {
        return create(event, startAt, status, venueId, null);
    }

    public static Show create(
            Event event,
            LocalDateTime startAt,
            ShowStatus status,
            Long venueId,
            Long createdByUserId
    ) {
        return Show.builder()
                .event(event)
                .startAt(startAt)
                .status(status)
                .createdAt(LocalDateTime.now())
                .venueId(venueId)
                .createdByUserId(createdByUserId)
                .build();
    }

    public boolean isHeldAt(Long venueId) {
        return this.venueId.equals(venueId);
    }

    @Builder
    public Show(
            Long id,
            LocalDateTime startAt,
            ShowStatus status,
            LocalDateTime createdAt,
            Event event,
            Long venueId,
            Long createdByUserId
    ) {
        this.id = id;
        this.startAt = startAt;
        this.status = status;
        this.createdAt = createdAt;
        this.event = event;
        this.venueId = venueId;
        this.createdByUserId = createdByUserId;
    }
}
