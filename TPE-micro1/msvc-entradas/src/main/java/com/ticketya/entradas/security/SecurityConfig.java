package com.ticketya.entradas.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean JwtDecoder jwtDecoder(@Value("${ticketya.jwt.public-key:file:../.secrets/jwt-public.pem}") Resource key,
            @Value("${ticketya.jwt.issuer:ticketya-usuarios}") String issuer,
            @Value("${ticketya.jwt.audience:ticketya-api}") String audience) {
        return JwtSupport.decoder(key, issuer, audience);
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/entradas/emitir").hasAuthority("SERVICE_compras")
                .requestMatchers(HttpMethod.POST, "/entradas/evento/*/invalidar").hasAnyAuthority("SERVICE_eventos", "SERVICE_compras")
                .requestMatchers(HttpMethod.GET, "/entradas/*", "/entradas/cliente/*").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR", "SERVICE_compras")
                .requestMatchers(HttpMethod.POST, "/devoluciones").hasRole("CLIENTE")
                .requestMatchers(HttpMethod.GET, "/devoluciones/pendientes").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH, "/devoluciones/*/aprobar", "/devoluciones/*/rechazar").hasRole("ADMINISTRADOR")
                .anyRequest().denyAll())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(JwtSupport.authenticationConverter())))
            .build();
    }
}
