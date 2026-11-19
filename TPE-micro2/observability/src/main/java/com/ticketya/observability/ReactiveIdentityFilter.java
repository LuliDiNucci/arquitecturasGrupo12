package com.ticketya.observability;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

public final class ReactiveIdentityFilter implements WebFilter {
    @Override public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(context -> HttpEvents.user(context.getAuthentication())).defaultIfEmpty("anonimo")
            .flatMap(user -> {
                exchange.getAttributes().put(HttpEvents.USER_ATTRIBUTE, user);
                return chain.filter(exchange);
            });
    }
}
