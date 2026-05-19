package com.ticketinglab.hold.infrastructure.web;

import com.ticketinglab.hold.application.HoldRequestBulkhead;
import com.ticketinglab.hold.application.HoldRequestPermit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class HoldFastFailFilter extends OncePerRequestFilter {

    private static final String HOLD_CREATE_PATH = "/api/holds";
    private static final String HOLD_CREATE_METHOD = "POST";
    private static final byte[] TOO_MANY_REQUESTS_BODY = """
            {"status":429,"error":"Too Many Requests","message":"hold request capacity exceeded"}
            """.getBytes(StandardCharsets.UTF_8);

    private final HoldRequestBulkhead holdRequestBulkhead;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HOLD_CREATE_METHOD.equalsIgnoreCase(request.getMethod())
                || !HOLD_CREATE_PATH.equals(requestPath(request));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<HoldRequestPermit> permit = holdRequestBulkhead.tryAcquire();
        if (permit.isEmpty()) {
            reject(response);
            return;
        }

        try (HoldRequestPermit ignored = permit.get()) {
            filterChain.doFilter(request, response);
        }
    }

    private String requestPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (contextPath == null || contextPath.isBlank()) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, "1");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(TOO_MANY_REQUESTS_BODY);
    }
}
