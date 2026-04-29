package com.ticketinglab.auth.infrastructure.jwt;

import com.ticketinglab.auth.domain.TokenSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final TokenSessionRepository tokenSessionRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, TokenSessionRepository tokenSessionRepository) {
        this.tokenProvider = tokenProvider;
        this.tokenSessionRepository = tokenSessionRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveBearerToken(request);

        if (token != null && tokenProvider.isValidAccessToken(token) && isCurrentAccessToken(token)) {
            SecurityContextHolder.getContext()
                    .setAuthentication(tokenProvider.getAuthentication(token));
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header)) return null;
        if (!header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }

    private boolean isCurrentAccessToken(String token) {
        Long userId = tokenProvider.getUserId(token);
        return tokenSessionRepository.findByUserId(userId)
                .map(tokenSession -> tokenSession.hasAccessToken(token))
                .orElse(false);
    }
}
