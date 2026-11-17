package com.ticketya.usuarios.services.impl;

import com.ticketya.usuarios.dto.TokenResponse;
import com.ticketya.usuarios.exceptions.ApiException;
import com.ticketya.usuarios.security.LoginAttemptLimiter;
import com.ticketya.usuarios.services.*;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class ServiceAuthenticationServiceImpl implements ServiceAuthenticationService {
    private final Map<String, String> credentials = new HashMap<>();
    private final TokenService tokens;
    private final LoginAttemptLimiter limiter;
    public ServiceAuthenticationServiceImpl(Environment env, TokenService tokens, LoginAttemptLimiter limiter) {
        this.tokens = tokens; this.limiter = limiter;
        for (String name : List.of("eventos", "reservas", "compras", "entradas")) {
            String secret = env.getProperty("ticketya.services." + name, "");
            if (!secret.isBlank() && secret.length() < 32)
                throw new IllegalArgumentException("Los secretos de servicios requieren al menos 32 caracteres");
            if (!secret.isBlank()) credentials.put(name, secret);
        }
    }
    @Override public TokenResponse authenticate(String authorization) {
        String id = "";
        String secret = "";
        try {
            if (authorization != null && authorization.startsWith("Basic ")) {
                String decoded = new String(Base64.getDecoder().decode(authorization.substring(6)), StandardCharsets.UTF_8);
                int colon = decoded.indexOf(':');
                if (colon > 0) { id = decoded.substring(0, colon); secret = decoded.substring(colon + 1); }
            }
        } catch (IllegalArgumentException ignored) { /* Respuesta uniforme */ }
        limiter.acquire("service:" + id);
        String expected = credentials.getOrDefault(id, "");
        if (expected.isBlank() || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8)))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        limiter.success("service:" + id);
        return tokens.forService(id);
    }
}
