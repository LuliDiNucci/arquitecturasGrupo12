package com.ticketya.compras.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    @Bean
    JwtDecoder jwtDecoder(@Value("${ticketya.jwt.public-key:file:../.secrets/jwt-public.pem}") Resource key,
            @Value("${ticketya.jwt.issuer:ticketya-usuarios}") String issuer,
            @Value("${ticketya.jwt.audience:ticketya-api}") String audience) {
        return JwtSupport.decoder(key, issuer, audience);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(new com.ticketya.observability.HttpIdentityFilter(),
                    org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(a -> a
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/compras").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/compras/*/confirmacion")
                        .hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR", "SERVICE_entradas")
                        .requestMatchers(HttpMethod.GET, "/compras/*", "/compras/*/reintegros")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/compras/*/pagar", "/compras/*/confirmar",
                                "/compras/*/reintentar-liberacion")
                        .hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/compras/reintegros").hasAuthority("SERVICE_entradas")
                        .requestMatchers(HttpMethod.GET, "/compras/evento/*/reporte").hasAuthority("SERVICE_eventos")
                        .requestMatchers(HttpMethod.GET, "/compras/evento/*/compradores")
                        .hasAuthority("SERVICE_eventos")
                        .requestMatchers(HttpMethod.POST, "/compras/evento/*/cancelar").hasAuthority("SERVICE_eventos")
                        .requestMatchers(HttpMethod.POST, "/compras/*/compensar").hasRole("ADMINISTRADOR")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/compras/cliente/*/eventos-recomendados")
                        .hasRole("CLIENTE")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(
                        o -> o.jwt(j -> j.jwtAuthenticationConverter(JwtSupport.authenticationConverter())))
                .build();
    }
}

