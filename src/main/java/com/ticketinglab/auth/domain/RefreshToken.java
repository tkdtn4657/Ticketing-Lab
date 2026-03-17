package com.ticketinglab.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(name = "token", nullable = false, length = 1000)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private RefreshToken(String token, Long userId, LocalDateTime createdAt) {
        this.token = token;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static RefreshToken issue(String token, Long userId) {
        return new RefreshToken(token, userId, LocalDateTime.now());
    }
}