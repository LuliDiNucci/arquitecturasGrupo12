package com.ticketya.reservas.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Contrato de tokens TicketYa. Se mantiene local a cada Maven para que los
 * servicios Boot 3 y Boot 4 puedan compilar y desplegarse independientemente.
 */
public final class JwtSupport {
    private static final Set<String> ROLES = Set.of("CLIENTE", "ORGANIZADOR", "ADMINISTRADOR", "PERSONAL_ACCESO");
    private static final Set<String> SERVICES = Set.of("eventos", "reservas", "compras", "entradas");
    private JwtSupport() {}

    public static RSAPublicKey readPublicKey(Resource resource) {
        try (var input = resource.getInputStream()) {
            String pem = new String(input.readAllBytes(), StandardCharsets.US_ASCII);
            byte[] der = Base64.getMimeDecoder().decode(pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", ""));
            RSAPublicKey key = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
            if (key.getModulus().bitLength() < 2048) throw new IllegalArgumentException("RSA requiere al menos 2048 bits");
            return key;
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo cargar la clave pública RSA configurada", ex);
        }
    }

    public static OAuth2TokenValidator<Jwt> validator(String issuer, String audience) {
        return new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator(issuer),
            jwt -> {
                try {
                    Instant now = Instant.now();
                    boolean time = jwt.getIssuedAt() != null && jwt.getExpiresAt() != null
                        && !jwt.getIssuedAt().isAfter(now.plusSeconds(30))
                        && jwt.getExpiresAt().isAfter(jwt.getIssuedAt());
                    boolean base = time && jwt.getAudience().contains(audience)
                        && jwt.getId() != null && !jwt.getId().isBlank()
                        && "RS256".equals(jwt.getHeaders().get("alg"));
                    Object raw = jwt.getClaims().get("roles");
                    if (!base || !(raw instanceof List<?> roles)) return invalid();
                    String use = jwt.getClaimAsString("token_use");
                    String subject = jwt.getSubject();
                    if ("user".equals(use)) {
                        if (subject == null || !subject.matches("[1-9][0-9]*")
                            || Long.parseLong(subject) <= 0 || roles.isEmpty()
                            || !roles.stream().allMatch(r -> r instanceof String && ROLES.contains(r))) return invalid();
                    } else if ("service".equals(use)) {
                        if (subject == null || !subject.startsWith("service:")
                            || !SERVICES.contains(subject.substring(8)) || !roles.isEmpty()) return invalid();
                    } else return invalid();
                    return OAuth2TokenValidatorResult.success();
                } catch (RuntimeException ex) {
                    return invalid();
                }
            });
    }
    private static OAuth2TokenValidatorResult invalid() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Claims JWT inválidas", null));
    }
    public static NimbusJwtDecoder decoder(Resource publicKey, String issuer, String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(readPublicKey(publicKey))
            .signatureAlgorithm(SignatureAlgorithm.RS256).build();
        decoder.setJwtValidator(validator(issuer, audience));
        return decoder;
    }
    public static JwtAuthenticationConverter authenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(JwtSupport::authorities);
        return converter;
    }
    public static Collection<GrantedAuthority> authorities(Jwt jwt) {
        List<GrantedAuthority> result = new ArrayList<>();
        if ("service".equals(jwt.getClaimAsString("token_use"))) {
            result.add(new SimpleGrantedAuthority("SERVICE_" + jwt.getSubject().substring(8)));
        } else {
            result.add(new SimpleGrantedAuthority("USER"));
            for (String role : jwt.getClaimAsStringList("roles")) {
                result.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
        return result;
    }
}
