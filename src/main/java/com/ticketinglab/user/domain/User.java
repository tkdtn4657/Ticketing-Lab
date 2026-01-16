package com.ticketinglab.user.domain;

import com.ticketinglab.auth.presentation.dto.SignupResponse;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role")
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static User createUser(SignupResponse dto) {
        return User.builder()
                .email(dto.email())
                .passwordHash(dto.password())
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Builder
    public User(Long id, String email, String passwordHash, String role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }
}
