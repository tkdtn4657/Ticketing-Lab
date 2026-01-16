package com.ticketinglab.auth.infrastructure.jwt;

import com.ticketinglab.auth.presentation.dto.TokenPair;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpMin;
    private final long refreshExpDays;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.access-token-exp-min}") long accessExpMin,
            @Value("${jwt.refresh-token-exp-days}") long refreshExpDays
    ) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.accessExpMin = accessExpMin;
        this.refreshExpDays = refreshExpDays;
    }

    public TokenPair createTokens(Long userId, String email, String role){
        Instant now = Instant.now();

        String accessToken = createAccessToken(userId, email, role);
        String refreshToken = createRefreshToken(userId);

        return new TokenPair(accessToken, refreshToken);
    }

    public String createAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpMin * 60)))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpDays * 24 * 3600)))
                .signWith(key)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();

        String userId = claims.getSubject();
        String role = String.valueOf(claims.get("role", String.class));

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }
}
