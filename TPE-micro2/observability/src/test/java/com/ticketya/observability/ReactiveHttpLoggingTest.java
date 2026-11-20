package com.ticketya.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.junit.jupiter.api.*;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

class ReactiveHttpLoggingTest {
    private final ListAppender<ILoggingEvent> events = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger("ticketya.http");
    @BeforeEach void capture() { events.list = new CopyOnWriteArrayList<>(); events.start(); logger.addAppender(events); MDC.clear(); }
    @AfterEach void cleanup() { logger.detachAppender(events); MDC.clear(); }
    @Test void preservesCorrelationAcrossSchedulerSwitchWithoutThreadLocalLeakage() {
        var filter = new ReactiveHttpLoggingFilter("msvc-gateway");
        var requests = new ArrayList<Mono<Void>>();
        for (int i = 1; i <= 16; i++) {
            String id = "operation-" + i;
            String user = Integer.toString(i);
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/compras/1?password=secret").header(Correlation.HEADER, id));
            Jwt jwt = Jwt.withTokenValue("secret").header("alg", "RS256").subject(user).build();
            requests.add(filter.filter(exchange, ex -> new ReactiveIdentityFilter().filter(ex,
                downstream -> Mono.deferContextual(context -> {
                    assertThat(context.<String>get(Correlation.KEY)).isEqualTo(id);
                    assertThat(downstream.getRequest().getHeaders().getFirst(Correlation.HEADER)).isEqualTo(id);
                    assertThat(MDC.get(Correlation.KEY)).isNull();
                    downstream.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return downstream.getResponse().setComplete();
                }).subscribeOn(Schedulers.parallel()))
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new JwtAuthenticationToken(jwt, List.of())))));
        }
        Mono.when(requests).block(Duration.ofSeconds(5));
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> assertThat(events.list).hasSize(16));
        for (var event : events.list) {
            var fields = event.getKeyValuePairs().stream().collect(Collectors.toMap(p -> p.key, p -> p.value));
            assertThat(fields.get(Correlation.KEY)).isEqualTo("operation-" + fields.get("usuario"));
            assertThat(fields).containsEntry("status", 403).containsEntry("endpoint", "/compras/1");
            assertThat(fields.toString()).doesNotContain("secret");
        }
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
    @Test void logsErrorAndStillReturnsValidatedHeader() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/broken").header(Correlation.HEADER, "invalid value"));
        assertThatThrownBy(() -> new ReactiveHttpLoggingFilter("gateway").filter(exchange,
            ex -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "NEVER_LOG_THIS"))).block())
            .isInstanceOf(ResponseStatusException.class);
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> assertThat(events.list).hasSize(1));
        UUID.fromString(exchange.getResponse().getHeaders().getFirst(Correlation.HEADER));
        var fields = events.list.getFirst().getKeyValuePairs().stream().collect(Collectors.toMap(p -> p.key, p -> p.value));
        assertThat(fields).containsEntry("status", 400).containsEntry("error", "ResponseStatusException");
        assertThat(fields.toString()).doesNotContain("NEVER_LOG_THIS");
    }
}
