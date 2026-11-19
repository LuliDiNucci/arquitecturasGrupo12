package com.ticketya.recomendaciones.security;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public JwtDecoder jwtDecoder(
                        @Value("${ticketya.jwt.public-key}") Resource publicKey,
                        @Value("${ticketya.jwt.issuer}") String issuer,
                        @Value("${ticketya.jwt.audience}") String audience)
                        throws IOException {

                RSAPublicKey key = (RSAPublicKey) RsaKeyConverters.x509()
                                .convert(publicKey.getInputStream());

                NimbusJwtDecoder decoder = NimbusJwtDecoder
                                .withPublicKey(key)
                                .build();

                OAuth2TokenValidator<Jwt> validarAudiencia = jwt -> jwt.getAudience().contains(audience)
                                ? OAuth2TokenValidatorResult.success()
                                : OAuth2TokenValidatorResult.failure(
                                                new org.springframework.security.oauth2.core.OAuth2Error(
                                                                "invalid_token",
                                                                "Audiencia JWT inválida",
                                                                null));

                decoder.setJwtValidator(
                                new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                                                JwtValidators.createDefaultWithIssuer(issuer),
                                                validarAudiencia));

                return decoder;
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

                converter.setJwtGrantedAuthoritiesConverter(jwt -> {

                        List<GrantedAuthority> authorities = new ArrayList<>();
                        if ("user".equals(jwt.getClaimAsString("token_use"))) {

                                List<String> roles = jwt.getClaimAsStringList("roles");

                                if (roles != null) {
                                        for (String rol : roles) {
                                                authorities.add(
                                                                new SimpleGrantedAuthority("ROLE_" + rol));
                                        }
                                }
                        }

                        return authorities;
                });

                return converter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter converter) throws Exception {

                return http
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/actuator/health")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/preferencias/**",
                                                                "/api/recomendaciones/**")
                                                .hasAnyRole("CLIENTE", "ADMINISTRADOR")

                                                .anyRequest().denyAll())

                                .oauth2ResourceServer(
                                                oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))

                                .build();
        }
}