package com.ticketya.usuarios.config;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.ticketya.usuarios.security.JwtSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;

@Configuration
public class JwtConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean
    JwtEncoder jwtEncoder(@Value("${ticketya.jwt.public-key}") Resource publicResource,
            @Value("${ticketya.jwt.private-key}") Resource privateResource) {
        try (var input = privateResource.getInputStream()) {
            String pem = new String(input.readAllBytes(), StandardCharsets.US_ASCII);
            byte[] der = Base64.getMimeDecoder().decode(pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", ""));
            RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
            var publicKey = JwtSupport.readPublicKey(publicResource);
            if (!privateKey.getModulus().equals(publicKey.getModulus())) {
                throw new IllegalArgumentException("Las claves RSA no forman un par");
            }
            RSAKey jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("ticketya-rsa").build();
            return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(jwk)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo cargar el par RSA configurado (PKCS#8 / X.509)", ex);
        }
    }
}
