package com.ticketya.notificaciones.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
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
    @Bean JwtDecoder jwtDecoder(@Value("${ticketya.jwt.public-key}") Resource key,
            @Value("${ticketya.jwt.issuer}") String issuer, @Value("${ticketya.jwt.audience}") String audience) {
        return JwtSupport.decoder(key, issuer, audience);
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable()).httpBasic(b -> b.disable()).formLogin(f -> f.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterAfter(new com.ticketya.observability.HttpIdentityFilter(),
                org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
            .authorizeHttpRequests(a -> a
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/notificaciones/compras-confirmadas").hasAuthority("SERVICE_compras")
                .requestMatchers(HttpMethod.POST, "/notificaciones/eventos-cancelados", "/notificaciones/eventos-modificados",
                    "/notificaciones/artistas-favoritos").hasAuthority("SERVICE_eventos")
                .requestMatchers(HttpMethod.GET, "/notificaciones/*").hasAnyAuthority("USER", "SERVICE_compras", "SERVICE_eventos")
                .anyRequest().denyAll())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(JwtSupport.authenticationConverter())))
            .build();
    }
}
