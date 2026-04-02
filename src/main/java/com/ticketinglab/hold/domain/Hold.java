package com.ticketinglab.hold.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "holds")
public class Hold {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HoldStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @OrderBy("id asc")
    @OneToMany(mappedBy = "hold", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoldItem> items = new ArrayList<>();

    private Hold(
            String id,
            HoldStatus status,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            Long userId,
            Long showId
    ) {
        this.id = id;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.userId = userId;
        this.showId = showId;
    }

    public static Hold create(Long userId, Long showId, LocalDateTime expiresAt) {
        return new Hold(
                UUID.randomUUID().toString(),
                HoldStatus.ACTIVE,
                expiresAt,
                LocalDateTime.now(),
                userId,
                showId
        );
    }

    public void addSeatItem(Long seatId, int unitPrice) {
        items.add(HoldItem.seat(this, seatId, unitPrice));
    }

    public void addSectionItem(Long sectionId, int quantity, int unitPrice) {
        items.add(HoldItem.section(this, sectionId, quantity, unitPrice));
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isActive() {
        return status == HoldStatus.ACTIVE;
    }

    public boolean isExpiredAt(LocalDateTime at) {
        return !expiresAt.isAfter(at);
    }

    public void expire(LocalDateTime at) {
        if (!isActive()) {
            return;
        }
        if (!isExpiredAt(at)) {
            throw new IllegalStateException("hold not expired");
        }
        this.status = HoldStatus.EXPIRED;
    }

    public void cancel() {
        if (status == HoldStatus.CONVERTED) {
            throw new IllegalStateException("converted hold cannot be canceled");
        }
        if (status == HoldStatus.ACTIVE) {
            this.status = HoldStatus.CANCELED;
        }
    }

    public void convert() {
        if (status == HoldStatus.CONVERTED) {
            throw new IllegalStateException("hold already converted");
        }
        if (status != HoldStatus.ACTIVE) {
            throw new IllegalStateException("hold not active");
        }
        this.status = HoldStatus.CONVERTED;
    }
}