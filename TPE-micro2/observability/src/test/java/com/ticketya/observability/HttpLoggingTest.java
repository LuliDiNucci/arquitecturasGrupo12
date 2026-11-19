package com.ticketya.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import static org.assertj.core.api.Assertions.*;

class HttpLoggingTest {
    private final ListAppender<ILoggingEvent> events = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger("ticketya.http");
    private final ServletHttpLoggingFilter filter = new ServletHttpLoggingFilter("msvc-compras");
    @BeforeEach void capture() { MDC.clear(); events.start(); logger.addAppender(events); }
    @AfterEach void cleanup() { logger.detachAppender(events); MDC.clear(); SecurityContextHolder.clearContext(); }

    private Map<String, Object> fields() {
        assertThat(events.list).hasSize(1);
        return events.list.getFirst().getKeyValuePairs().stream().collect(Collectors.toMap(p -> p.key, p -> p.value));
    }
    @ParameterizedTest @ValueSource(ints = {200, 201, 400, 401, 403, 500})
    void logsAllResponsesOnceAndRestoresMdc(int status) throws Exception {
        var request = new MockHttpServletRequest("POST", "/compras");
        request.addHeader(Correlation.HEADER, "compra-123");
        request.addHeader("X-Usuario-Id", "999");
        request.addHeader("Authorization", "Bearer NEVER_LOG_THIS");
        request.setQueryString("password=NEVER_LOG_THIS");
        var response = new MockHttpServletResponse();
        MDC.put("previous", "preserved");
        filter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get(Correlation.KEY)).isEqualTo("compra-123");
            assertThat(((jakarta.servlet.http.HttpServletRequest) req).getHeader(Correlation.HEADER)).isEqualTo("compra-123");
            response.setStatus(status);
        });
        assertThat(response.getHeader(Correlation.HEADER)).isEqualTo("compra-123");
        assertThat(fields()).containsEntry("status", status).containsEntry("usuario", "anonimo")
            .containsEntry("endpoint", "/compras").containsEntry("correlationId", "compra-123");
        assertThat((Long) fields().get("duracionMs")).isNotNegative();
        assertThat(fields().toString()).doesNotContain("NEVER_LOG_THIS", "999");
        if (status >= 400) assertThat(fields()).containsEntry("error", "HTTP_" + status);
        assertThat(MDC.getCopyOfContextMap()).containsExactlyEntriesOf(Map.of("previous", "preserved"));
    }
    @ParameterizedTest @ValueSource(strings = {"", "with space", "header\r\ninjection", "a,b", "../../etc", "á", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void replacesUnsafeCorrelationIds(String incoming) throws Exception {
        var request = new MockHttpServletRequest("GET", "/compras/1");
        request.addHeader(Correlation.HEADER, incoming);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {});
        assertThat(UUID.fromString(response.getHeader(Correlation.HEADER))).isNotNull();
        assertThat(fields().get("correlationId")).isEqualTo(response.getHeader(Correlation.HEADER));
        assertThat(MDC.get(Correlation.KEY)).isNull();
    }
    @Test void rejectsMultipleHeadersAndSeparatesConsecutiveRequests() throws Exception {
        var first = new MockHttpServletRequest("GET", "/a");
        first.addHeader(Correlation.HEADER, "one"); first.addHeader(Correlation.HEADER, "two");
        var response = new MockHttpServletResponse();
        filter.doFilter(first, response, (req, res) -> {});
        UUID.fromString(response.getHeader(Correlation.HEADER));
        var next = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/b"), next, (req, res) -> {});
        assertThat(next.getHeader(Correlation.HEADER)).isNotEqualTo(response.getHeader(Correlation.HEADER));
        assertThat(events.list).hasSize(2);
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
    @Test void snapshotsValidatedPrincipalBeforeSecurityContextIsCleared() throws Exception {
        var request = new MockHttpServletRequest("POST", "/compras");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {
            Jwt jwt = Jwt.withTokenValue("NEVER_LOG_THIS").header("alg", "RS256").subject("7").build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
            new HttpIdentityFilter().doFilter(req, res, (r, s) -> response.setStatus(403));
            SecurityContextHolder.clearContext();
        });
        assertThat(fields()).containsEntry("usuario", "7").containsEntry("status", 403);
    }
    @Test void capturesUnhandledFailureWithoutLeakingMessageAndCleansContext() {
        var request = new MockHttpServletRequest("POST", "/compras");
        assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            throw new ServletException("password=NEVER_LOG_THIS");
        })).isInstanceOf(ServletException.class);
        assertThat(fields()).containsEntry("status", 500).containsEntry("error", "ServletException");
        assertThat(fields().toString()).doesNotContain("NEVER_LOG_THIS");
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
    @Test void waitsUntilServletAsyncCompletionAndDoesNotDuplicateLog() throws Exception {
        var request = new MockHttpServletRequest("GET", "/async"); request.setAsyncSupported(true);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> request.startAsync(req, res));
        assertThat(events.list).isEmpty();
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
        request.getAsyncContext().complete();
        assertThat(fields()).containsEntry("status", 200);
    }
    @Test void restoresNestedBackgroundContextsEvenAfterFailure() {
        MDC.put(Correlation.KEY, "outer"); MDC.put("usuario", "7");
        assertThatThrownBy(() -> {
            try (var scope = Correlation.open("persisted-job")) {
                assertThat(MDC.get(Correlation.KEY)).isEqualTo("persisted-job");
                assertThat(MDC.get("usuario")).isNull();
                throw new IllegalStateException("controlled failure");
            }
        }).isInstanceOf(IllegalStateException.class);
        assertThat(MDC.get(Correlation.KEY)).isEqualTo("outer");
        assertThat(MDC.get("usuario")).isEqualTo("7");
    }
}
