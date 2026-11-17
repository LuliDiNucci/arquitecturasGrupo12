package com.ticketya.usuarios.security;

import com.ticketya.usuarios.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.*;

@Component
public class LoginAttemptLimiter {
    private final Map<String, Attempts> attempts = new HashMap<>();
    private final Clock clock;
    private final int max;
    private final Duration window;
    public LoginAttemptLimiter(Clock clock, @Value("${ticketya.login.max-attempts:5}") int max,
            @Value("${ticketya.login.window:PT15M}") Duration window) {
        if (max < 1 || window.isNegative() || window.isZero()) throw new IllegalArgumentException("Límite de login inválido");
        this.clock = clock; this.max = max; this.window = window;
    }
    public synchronized void acquire(String key) {
        Instant now = clock.instant();
        attempts.entrySet().removeIf(e -> !e.getValue().until.isAfter(now));
        Attempts entry = attempts.get(key);
        if ((entry != null && entry.count >= max) || (entry == null && attempts.size() >= 10000)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos; reintente más tarde");
        }
        if (entry == null) { entry = new Attempts(now.plus(window)); attempts.put(key, entry); }
        entry.count++;
    }
    public synchronized void success(String key) { attempts.remove(key); }
    private static class Attempts {
        private final Instant until;
        private int count;
        private Attempts(Instant until) { this.until = until; }
    }
}
