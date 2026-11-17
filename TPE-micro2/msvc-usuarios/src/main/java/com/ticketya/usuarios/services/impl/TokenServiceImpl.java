package com.ticketya.usuarios.services.impl;

import com.ticketya.usuarios.dto.*;
import com.ticketya.usuarios.entities.Usuario;
import com.ticketya.usuarios.services.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class TokenServiceImpl implements TokenService {
    private final JwtEncoder encoder;
    private final Clock clock;
    private final String issuer;
    private final String audience;
    private final Duration ttl;
    public TokenServiceImpl(JwtEncoder encoder, Clock clock,
            @Value("${ticketya.jwt.issuer}") String issuer,
            @Value("${ticketya.jwt.audience}") String audience,
            @Value("${ticketya.jwt.ttl:PT60M}") Duration ttl) {
        if (ttl.isNegative() || ttl.isZero() || ttl.compareTo(Duration.ofDays(1)) > 0)
            throw new IllegalArgumentException("JWT_TTL debe ser mayor a cero y como máximo 24 horas");
        this.encoder = encoder; this.clock = clock; this.issuer = issuer; this.audience = audience; this.ttl = ttl;
    }
    @Override public TokenResponse forUser(Usuario usuario) {
        return issue(usuario.getId().toString(), "user",
            usuario.getRoles().stream().map(Enum::name).sorted().toList(), ttl, new UsuarioResponse(usuario));
    }
    @Override public TokenResponse forService(String clientId) {
        return issue("service:" + clientId, "service", List.of(), Duration.ofMinutes(5), null);
    }
    private TokenResponse issue(String subject, String use, List<String> roles, Duration duration, UsuarioResponse usuario) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder().subject(subject).issuer(issuer).audience(List.of(audience))
            .issuedAt(now).expiresAt(now.plus(duration)).id(UUID.randomUUID().toString())
            .claim("roles", roles).claim("token_use", use).build();
        String value = encoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).keyId("ticketya-rsa").build(), claims)).getTokenValue();
        return new TokenResponse(value, now.plus(duration), duration.toSeconds(), usuario);
    }
}
