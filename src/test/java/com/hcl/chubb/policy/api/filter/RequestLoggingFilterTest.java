package com.hcl.chubb.policy.api.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    private String runWithHeader(String headerValue, AtomicReference<String> mdcDuringRequest,
                                 MockHttpServletResponse response) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/policies");
        if (headerValue != null) {
            request.addHeader(RequestLoggingFilter.REQUEST_ID_HEADER, headerValue);
        }
        FilterChain chain = (req, res) -> mdcDuringRequest.set(MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY));
        filter.doFilter(request, response, chain);
        return response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER);
    }

    @Test
    void generatesRequestIdWhenHeaderMissing() throws Exception {
        AtomicReference<String> mdc = new AtomicReference<>();

        String requestId = runWithHeader(null, mdc, new MockHttpServletResponse());

        assertThat(requestId).isNotBlank();
        assertThat(mdc.get()).isEqualTo(requestId);
    }

    @Test
    void reusesWellFormedClientRequestId() throws Exception {
        AtomicReference<String> mdc = new AtomicReference<>();

        String requestId = runWithHeader("abc-123", mdc, new MockHttpServletResponse());

        assertThat(requestId).isEqualTo("abc-123");
        assertThat(mdc.get()).isEqualTo("abc-123");
    }

    @Test
    void replacesMalformedClientRequestId() throws Exception {
        AtomicReference<String> mdc = new AtomicReference<>();

        String requestId = runWithHeader("bad id\nforged-log-line", mdc, new MockHttpServletResponse());

        assertThat(requestId).isNotEqualTo("bad id\nforged-log-line").matches("^[A-Za-z0-9-]{1,64}$");
    }

    @Test
    void clearsMdcAfterRequest() throws Exception {
        runWithHeader("abc-123", new AtomicReference<>(), new MockHttpServletResponse());

        assertThat(MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void clearsMdcWhenChainFails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/boom");
        FilterChain failing = (req, res) -> {
            throw new IllegalStateException("boom");
        };

        assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(), failing))
                .isInstanceOf(IllegalStateException.class);
        assertThat(MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY)).isNull();
    }
}
