package com.ticketya.usuarios.security;

import com.ticketya.usuarios.exceptions.ApiException;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginAttemptLimiterTest {
    @Test void windowExpiresAndSuccessResetsOnlyItsAccount() {
        Clock clock = mock(Clock.class);
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        when(clock.instant()).thenReturn(start);
        var limiter = new LoginAttemptLimiter(clock, 2, Duration.ofMinutes(15));
        limiter.acquire("one"); limiter.acquire("one");
        limiter.acquire("two"); limiter.acquire("two");
        assertThatThrownBy(() -> limiter.acquire("one")).isInstanceOf(ApiException.class);
        limiter.success("one");
        assertThatCode(() -> limiter.acquire("one")).doesNotThrowAnyException();
        assertThatThrownBy(() -> limiter.acquire("two")).isInstanceOf(ApiException.class);
        when(clock.instant()).thenReturn(start.plusSeconds(901));
        assertThatCode(() -> limiter.acquire("two")).doesNotThrowAnyException();
    }
}
