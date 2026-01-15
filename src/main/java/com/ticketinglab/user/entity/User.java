package com.ticketinglab.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@RequiredArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "email", unique = true)
    String email;

    @Column(name = "password_hash", unique = true)
    String password_hash;

    @Column(name = "role", unique = true)
    String role;

    @Column(name = "created_at", unique = true)
    LocalDateTime created_at;

}
