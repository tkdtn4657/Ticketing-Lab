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
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    public static Venue create(String code, String name, String address) {
        return create(code, name, address, null);
    }

    public static Venue create(String code, String name, String address, Long createdByUserId) {
        LocalDateTime now = LocalDateTime.now();
        return Venue.builder()
                .code(code)
                .name(name)
                .address(address)
                .createdAt(now)
                .updatedAt(now)
                .createdByUserId(createdByUserId)
                .build();
    }

    public void updateInfo(String name, String address) {
        this.name = name;
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignCreatorIfMissing(Long userId) {
        if (createdByUserId == null) {
            this.createdByUserId = userId;
        }
    }

    public boolean isCreatorMissing() {
        return createdByUserId == null;
    }

    public boolean isCreatedBy(Long userId) {
        return Objects.equals(createdByUserId, userId);
    }

    @Builder
    public Venue(
            Long id,
            String code,
            String name,
            String address,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long createdByUserId
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdByUserId = createdByUserId;
    }
}
