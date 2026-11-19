package com.ticketya.observability;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

/** WebFilter exterior a Spring Security: también observa 401/403 y rutas sin destino. */
public final class ReactiveHttpLoggingFilter implements WebFilter {
    private final String service;
    public ReactiveHttpLoggingFilter(String service) { this.service = service; }
    @Override public Mono<Void> filter(ServerWebExchange original, WebFilterChain chain) {
        return Mono.defer(() -> {
            List<String> values = original.getRequest().getHeaders().get(Correlation.HEADER);
            String id = Correlation.validateOrNew(values != null && values.size() == 1 ? values.getFirst() : null);
            var exchange = original.mutate().request(r -> r.headers(h -> h.set(Correlation.HEADER, id))).build();
            exchange.getResponse().getHeaders().set(Correlation.HEADER, id);
            exchange.getResponse().beforeCommit(() -> {
                exchange.getResponse().getHeaders().set(Correlation.HEADER, id); return Mono.empty();
            });
            long started = System.nanoTime();
            var failure = new AtomicReference<Throwable>();
            return chain.filter(exchange).doOnError(failure::set).doFinally(signal -> {
                Throwable error = failure.get();
                var code = exchange.getResponse().getStatusCode();
                int status = error instanceof ResponseStatusException exception ? exception.getStatusCode().value()
                    : error != null ? 500 : code != null ? code.value() : signal == SignalType.CANCEL ? 499 : 200;
                HttpEvents.write("http_request", service, id,
                    exchange.getAttributeOrDefault(HttpEvents.USER_ATTRIBUTE, "anonimo"),
                    exchange.getRequest().getMethod().name(), HttpEvents.path(exchange.getRequest().getURI()),
                    status, started, error == null ? null : error.getClass().getSimpleName());
            }).contextWrite(context -> context.put(Correlation.KEY, id));
        });
    }
}
