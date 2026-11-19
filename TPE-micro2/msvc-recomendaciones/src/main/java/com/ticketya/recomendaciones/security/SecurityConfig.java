package com.ticketya.recomendaciones.security;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
                        @Value("${ticketya.jwt.audience}") String audience) {
                return JwtSupport.decoder(publicKey, issuer, audience);
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                return JwtSupport.authenticationConverter();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter converter) throws Exception {

                return http
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .addFilterAfter(new com.ticketya.observability.HttpIdentityFilter(),
                    org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
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
