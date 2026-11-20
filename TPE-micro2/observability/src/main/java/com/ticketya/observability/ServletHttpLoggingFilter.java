package com.ticketya.observability;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

public final class ServletHttpLoggingFilter extends OncePerRequestFilter {
    private static final String STATE = ServletHttpLoggingFilter.class.getName();
    private final String service;
    public ServletHttpLoggingFilter(String service) { this.service = service; }
    @Override protected boolean shouldNotFilterAsyncDispatch() { return false; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        State state = (State) request.getAttribute(STATE);
        if (state == null) {
            List<String> values = Collections.list(request.getHeaders(Correlation.HEADER));
            state = new State(Correlation.validateOrNew(values.size() == 1 ? values.getFirst() : null));
            request.setAttribute(STATE, state);
        }
        State current = state;
        response.setHeader(Correlation.HEADER, current.id);
        HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
            @Override public String getHeader(String name) {
                return Correlation.HEADER.equalsIgnoreCase(name) ? current.id : super.getHeader(name);
            }
            @Override public Enumeration<String> getHeaders(String name) {
                return Correlation.HEADER.equalsIgnoreCase(name)
                    ? Collections.enumeration(List.of(current.id)) : super.getHeaders(name);
            }
            @Override public Enumeration<String> getHeaderNames() {
                var names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                names.addAll(Collections.list(super.getHeaderNames())); names.add(Correlation.HEADER);
                return Collections.enumeration(names);
            }
        };
        try (var scope = Correlation.open(current.id)) {
            try { chain.doFilter(wrapped, response); }
            catch (IOException | ServletException | RuntimeException ex) {
                current.failure = ex.getClass().getSimpleName();
                current.failed = true;
                throw ex;
            } finally {
                if (request.isAsyncStarted()) {
                    if (current.listening.compareAndSet(false, true)) request.getAsyncContext().addListener(new AsyncListener() {
                        @Override public void onComplete(AsyncEvent event) { complete(request, response, current); }
                        @Override public void onTimeout(AsyncEvent event) { current.failed = true; current.failure = "AsyncTimeout"; }
                        @Override public void onError(AsyncEvent event) {
                            current.failed = true; current.failure = event.getThrowable().getClass().getSimpleName();
                        }
                        @Override public void onStartAsync(AsyncEvent event) { event.getAsyncContext().addListener(this); }
                    });
                } else complete(request, response, current);
            }
        }
    }
    private void complete(HttpServletRequest request, HttpServletResponse response, State state) {
        if (!state.logged.compareAndSet(false, true)) return;
        Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        Object user = request.getAttribute(HttpEvents.USER_ATTRIBUTE);
        Object handled = request.getAttribute(HttpEvents.ERROR_ATTRIBUTE);
        HttpEvents.write("http_request", service, state.id, user == null ? "anonimo" : user.toString(),
            request.getMethod(), route == null ? HttpEvents.path(URI.create(request.getRequestURI())) : route.toString(),
            state.failed && response.getStatus() < 400 ? 500 : response.getStatus(), state.started,
            state.failure != null ? state.failure : handled == null ? null : handled.toString());
    }
    private static final class State {
        final String id;
        final long started = System.nanoTime();
        final AtomicBoolean logged = new AtomicBoolean();
        final AtomicBoolean listening = new AtomicBoolean();
        volatile String failure;
        volatile boolean failed;
        State(String id) { this.id = id; }
    }
}
