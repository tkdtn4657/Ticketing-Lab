package com.ticketinglab.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    public static Event create(String title, String description, EventStatus status) {
        return create(title, description, status, null);
    }

    public static Event create(String title, String description, EventStatus status, Long createdByUserId) {
        return Event.builder()
                .title(title)
                .description(description)
                .status(status)
                .createdAt(LocalDateTime.now())
                .createdByUserId(createdByUserId)
                .build();
    }

    public boolean hasStatus(EventStatus status) {
        return this.status == status;
    }

    @Builder
    public Event(
            Long id,
            String title,
            String description,
            EventStatus status,
            LocalDateTime createdAt,
            Long createdByUserId
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.createdByUserId = createdByUserId;
    }
}
