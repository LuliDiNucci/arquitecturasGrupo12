package com.ticketya.entradas.security;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

public final class TestTokens {
    public static final KeyPair KEYS = pair();
    public static final String PUBLIC = pem("PUBLIC KEY", KEYS.getPublic().getEncoded());
    public static final String PRIVATE = pem("PRIVATE KEY", KEYS.getPrivate().getEncoded());
    private TestTokens() {}
    public static KeyPair pair() {
        try { var generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048); return generator.generateKeyPair(); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    private static String pem(String name, byte[] data) {
        try {
            Path path = Files.createTempFile("ticketya-test-", ".pem");
            path.toFile().deleteOnExit();
            Files.writeString(path, "-----BEGIN " + name + "-----\n" + Base64.getEncoder().encodeToString(data)
                + "\n-----END " + name + "-----\n", StandardCharsets.US_ASCII);
            return path.toUri().toString();
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    public static String user(long id, String... roles) { return sign(claims(Long.toString(id), List.of(roles), "user"), KEYS); }
    public static String service(String name) { return sign(claims("service:" + name, List.of(), "service"), KEYS); }
    public static Map<String, Object> claims(String sub, List<String> roles, String use) {
        Map<String, Object> result = new HashMap<>();
        result.put("sub", sub); result.put("roles", roles); result.put("token_use", use);
        result.put("iss", "ticketya-usuarios"); result.put("aud", List.of("ticketya-api"));
        result.put("iat", Instant.now().minusSeconds(2)); result.put("exp", Instant.now().plusSeconds(600));
        result.put("jti", UUID.randomUUID().toString());
        return result;
    }
    public static String sign(Map<String, Object> claims, KeyPair pair) {
        RSAKey key = new RSAKey.Builder((RSAPublicKey) pair.getPublic()).privateKey((RSAPrivateKey) pair.getPrivate())
            .keyID("test").build();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(key)));
        JwtClaimsSet set = JwtClaimsSet.builder().claims(map -> map.putAll(claims)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), set)).getTokenValue();
    }
}
