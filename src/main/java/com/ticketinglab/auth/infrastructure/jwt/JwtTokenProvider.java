package com.ticketinglab.auth.infrastructure.jwt;

import com.ticketinglab.auth.presentation.dto.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

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

    public TokenPair createTokens(Long userId, String email, String role) {
        String accessToken = createAccessToken(userId, email, role);
        String refreshToken = createRefreshToken(userId);

        return new TokenPair(accessToken, refreshToken);
    }

    public String createAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpMin * 60)))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpDays * 24 * 3600)))
                .signWith(key)
                .compact();
    }

    public boolean isValidAccessToken(String token) {
        return validate(token, ACCESS_TOKEN_TYPE);
    }

    public boolean isValidRefreshToken(String token) {
        return validate(token, REFRESH_TOKEN_TYPE);
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String userId = claims.getSubject();
        String role = claims.get("role", String.class);

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }

    private boolean validate(String token, String expectedTokenType) {
        try {
            Claims claims = parseClaims(token);
            return expectedTokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}