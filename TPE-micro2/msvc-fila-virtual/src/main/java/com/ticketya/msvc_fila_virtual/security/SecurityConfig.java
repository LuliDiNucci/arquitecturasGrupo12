package com.ticketya.msvc_fila_virtual.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${ticketya.jwt.public-key}") Resource publicKey,
            @Value("${ticketya.jwt.issuer}") String issuer,
            @Value("${ticketya.jwt.audience}") String audience) {

        return JwtSupport.decoder(
                publicKey,
                issuer,
                audience);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Documentación
                        .requestMatchers(
                                HttpMethod.GET,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()

                        // Ingresar a la fila
                        .requestMatchers(
                                HttpMethod.POST,
                                "/filas/*/ingresar")
                        .hasRole("CLIENTE")

                        // Consultar posición
                        .requestMatchers(
                                HttpMethod.GET,
                                "/filas/*/clientes/*")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")

                        // Habilitar tandas
                        .requestMatchers(
                                HttpMethod.POST,
                                "/filas/*/habilitar")
                        .hasRole("ADMINISTRADOR")

                        // Validación interna de turnos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/filas/*/clientes/*/validar")
                        .hasAuthority("SERVICE_reservas")

                        // Finalización interna de turnos
                        .requestMatchers(
                                HttpMethod.POST,
                                "/filas/*/clientes/*/finalizar")
                        .hasAuthority("SERVICE_reservas")

                        .anyRequest().denyAll())

                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(
                        JwtSupport.authenticationConverter())))

                .build();
    }
}