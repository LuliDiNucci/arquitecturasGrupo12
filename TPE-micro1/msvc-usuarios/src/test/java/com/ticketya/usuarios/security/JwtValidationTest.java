package com.ticketya.usuarios.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.oauth2.jwt.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class JwtValidationTest {
    private final JwtDecoder decoder = JwtSupport.decoder(new DefaultResourceLoader().getResource(TestTokens.PUBLIC),
        "ticketya-usuarios", "ticketya-api");

    @Test void verifiesSignatureAndRoles() {
        Jwt jwt = decoder.decode(TestTokens.user(10, "CLIENTE", "ORGANIZADOR"));
        assertThat(jwt.getSubject()).isEqualTo("10");
        assertThat(JwtSupport.authorities(jwt)).extracting("authority")
            .containsExactly("USER", "ROLE_CLIENTE", "ROLE_ORGANIZADOR");
    }
    @ParameterizedTest @ValueSource(strings = {"expired", "issuer", "audience", "no-exp", "no-iat", "no-jti",
        "invalid-sub", "invalid-roles", "invalid-use", "future-iat", "service-with-human-role", "unknown-service"})
    void rejectsInvalidClaims(String kind) {
        Map<String, Object> claims = TestTokens.claims("10", List.of("CLIENTE"), "user");
        switch (kind) {
            case "expired" -> { claims.put("iat", Instant.now().minusSeconds(120)); claims.put("exp", Instant.now().minusSeconds(1)); }
            case "issuer" -> claims.put("iss", "otro");
            case "audience" -> claims.put("aud", List.of("otra"));
            case "no-exp" -> claims.remove("exp");
            case "no-iat" -> claims.remove("iat");
            case "no-jti" -> claims.remove("jti");
            case "invalid-sub" -> claims.put("sub", "9999999999999999999999999");
            case "invalid-roles" -> claims.put("roles", List.of("SUPERUSER"));
            case "invalid-use" -> claims.put("token_use", "other");
            case "future-iat" -> claims.put("iat", Instant.now().plusSeconds(100));
            case "service-with-human-role" -> { claims.put("sub", "service:compras"); claims.put("token_use", "service"); }
            case "unknown-service" -> { claims.put("sub", "service:desconocido"); claims.put("token_use", "service"); claims.put("roles", List.of()); }
        }
        assertThatThrownBy(() -> decoder.decode(TestTokens.sign(claims, TestTokens.KEYS))).isInstanceOf(JwtException.class);
    }
    @Test void rejectsUnauthorizedKeyAndAlteration() {
        assertThatThrownBy(() -> decoder.decode(TestTokens.sign(TestTokens.claims("10", List.of("CLIENTE"), "user"),
            TestTokens.pair()))).isInstanceOf(JwtException.class);
        String token = TestTokens.user(10, "CLIENTE");
        String[] parts = token.split("\\.");
        byte[] signature = Base64.getUrlDecoder().decode(parts[2]); signature[0] ^= 1;
        String altered = parts[0] + "." + parts[1] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        assertThatThrownBy(() -> decoder.decode(altered)).isInstanceOf(JwtException.class);
    }
    @Test void rejectsUnsignedAndHmacTokens() throws Exception {
        var claims = new JWTClaimsSet.Builder().subject("10").build();
        String unsigned = new PlainJWT(claims).serialize();
        assertThatThrownBy(() -> decoder.decode(unsigned)).isInstanceOf(JwtException.class);
        SignedJWT hmac = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        hmac.sign(new MACSigner(new byte[32]));
        assertThatThrownBy(() -> decoder.decode(hmac.serialize())).isInstanceOf(JwtException.class);
    }
    @Test void serviceIdentityDoesNotGrantHumanRoles() {
        assertThat(JwtSupport.authorities(decoder.decode(TestTokens.service("compras"))))
            .extracting("authority").containsExactly("SERVICE_compras");
    }
}
