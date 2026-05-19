package com.ticketinglab.hold.infrastructure.web;

import com.ticketinglab.hold.application.HoldRequestBulkhead;
import com.ticketinglab.hold.application.HoldRequestPermit;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HoldFastFailFilterTest {

    @Test
    @DisplayName("POST /api/holds가 아니면 bulkhead를 거치지 않는다")
    void doFilter_skipsNonHoldCreateRequests() throws Exception {
        HoldRequestBulkhead bulkhead = mock(HoldRequestBulkhead.class);
        FilterChain filterChain = mock(FilterChain.class);
        HoldFastFailFilter filter = new HoldFastFailFilter(bulkhead);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/holds");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(bulkhead, never()).tryAcquire();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("POST /api/holds에서 슬롯이 없으면 429로 즉시 거절한다")
    void doFilter_rejectsWhenBulkheadIsFull() throws Exception {
        HoldRequestBulkhead bulkhead = mock(HoldRequestBulkhead.class);
        FilterChain filterChain = mock(FilterChain.class);
        HoldFastFailFilter filter = new HoldFastFailFilter(bulkhead);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/holds");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(bulkhead.tryAcquire()).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("1");
        assertThat(response.getContentAsString()).contains("hold request capacity exceeded");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("POST /api/holds 처리 후 획득한 슬롯을 반환한다")
    void doFilter_releasesPermitAfterRequest() throws Exception {
        HoldRequestBulkhead bulkhead = mock(HoldRequestBulkhead.class);
        FilterChain filterChain = mock(FilterChain.class);
        HoldFastFailFilter filter = new HoldFastFailFilter(bulkhead);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/holds");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean released = new AtomicBoolean();

        when(bulkhead.tryAcquire())
                .thenReturn(Optional.of(HoldRequestPermit.acquired(() -> released.set(true))));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(released).isTrue();
    }
}
