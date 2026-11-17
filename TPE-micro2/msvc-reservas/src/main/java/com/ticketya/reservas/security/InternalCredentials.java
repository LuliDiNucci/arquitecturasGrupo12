package com.ticketya.reservas.security;

import java.time.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Propaga identidad en lecturas; las escrituras internas usan la identidad de este servicio. */
@Component
public class InternalCredentials {
    private final RestClient tokenClient;
    private final String secret;
    private String cachedToken;
    private Instant refreshAt = Instant.EPOCH;
    public InternalCredentials(@Value("${ticketya.service.secret:}") String secret,
            @Value("${ticketya.service.token-url:http://localhost:8085/auth/service-token}") String tokenUrl) {
        this.secret = secret;
        var factory = new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.tokenClient = RestClient.builder().requestFactory(factory).baseUrl(tokenUrl).build();
    }
    public String authorization(String method) {
        var auth = CurrentIdentity.authentication();
        if ("GET".equals(method) && auth != null && auth.getPrincipal() instanceof Jwt jwt
            && "user".equals(jwt.getClaimAsString("token_use"))) return "Bearer " + jwt.getTokenValue();
        return "Bearer " + serviceToken();
    }
    private synchronized String serviceToken() {
        if (cachedToken != null && Instant.now().isBefore(refreshAt)) return cachedToken;
        if (secret.length() < 32) throw new IllegalStateException("Configure SERVICE_CLIENT_SECRET para reservas");
        Map<?, ?> response = tokenClient.post().headers(h -> h.setBasicAuth("reservas", secret))
            .retrieve().body(Map.class);
        if (response == null || !(response.get("accessToken") instanceof String token)
            || !(response.get("expiresIn") instanceof Number expires) || expires.longValue() <= 30)
            throw new IllegalStateException("Respuesta de credencial interna inválida");
        cachedToken = token;
        refreshAt = Instant.now().plusSeconds(expires.longValue() - 30);
        return token;
    }
}
