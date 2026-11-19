package com.ticketya.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${ticketya.jwt.public-key}") Resource key,
            @Value("${ticketya.jwt.issuer}") String issuer, @Value("${ticketya.jwt.audience}") String audience) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withPublicKey(JwtSupport.readPublicKey(key))
                .build();
        decoder.setJwtValidator(JwtSupport.validator(issuer, audience));
        return decoder;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(c -> c.disable()).httpBasic(b -> b.disable()).formLogin(f -> f.disable())
                .addFilterAfter(new com.ticketya.observability.ReactiveIdentityFilter(),
                    org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(a -> a
                        .pathMatchers(HttpMethod.GET, "/actuator/health", "/swagger-ui.html", "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs/**", "/*/v3/api-docs")
                        .permitAll()
                        .pathMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/service-token")
                        .permitAll()
                        .pathMatchers(HttpMethod.PATCH, "/usuarios/*/roles", "/usuarios/*/estado")
                        .hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.GET, "/usuarios/**").hasAuthority("USER")
                        .pathMatchers(HttpMethod.GET, "/eventos", "/organizadores",
                                "/eventos/*/notificaciones-cancelacion")
                        .hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.GET, "/eventos/**", "/lugares/**", "/sectores/**", "/asientos/**",
                                "/participantes")
                        .permitAll()
                        .pathMatchers(HttpMethod.PATCH, "/organizadores/*/usuario", "/eventos/*/suspender",
                                "/eventos/*/cancelar")
                        .hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.POST, "/eventos").hasAnyRole("ORGANIZADOR", "ADMINISTRADOR")
                        .pathMatchers(HttpMethod.PUT, "/eventos/**").hasAnyRole("ORGANIZADOR", "ADMINISTRADOR")
                        .pathMatchers(HttpMethod.PATCH, "/eventos/**").hasAnyRole("ORGANIZADOR", "ADMINISTRADOR")
                        .pathMatchers(HttpMethod.POST, "/lugares", "/lugares/*/sectores", "/sectores/*/asientos",
                                "/organizadores", "/participantes")
                        .hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.GET, "/disponibilidad").permitAll()
                        .pathMatchers(HttpMethod.POST, "/reservas").hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/reservas/*")
                        .hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR", "SERVICE_compras", "SERVICE_entradas")
                        .pathMatchers(HttpMethod.POST, "/reservas/*/confirmar", "/reservas/*/liberar")
                        .hasAuthority("SERVICE_compras")
                        .pathMatchers(HttpMethod.POST, "/reservas/*/devolver-entrada").hasAuthority("SERVICE_entradas")
                        .pathMatchers(HttpMethod.POST, "/reservas/evento/*/habilitar", "/reservas/evento/*/cancelar")
                        .hasAuthority("SERVICE_eventos")
                        .pathMatchers(HttpMethod.POST, "/compras").hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/compras/*/confirmacion")
                        .hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR", "SERVICE_entradas")
                        .pathMatchers(HttpMethod.GET, "/compras/*", "/compras/*/reintegros")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .pathMatchers(HttpMethod.POST, "/compras/*/pagar", "/compras/*/confirmar",
                                "/compras/*/reintentar-liberacion")
                        .hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.POST, "/compras/reintegros").hasAuthority("SERVICE_entradas")
                        .pathMatchers(HttpMethod.POST, "/compras/evento/*/cancelar").hasAuthority("SERVICE_eventos")
                        .pathMatchers(HttpMethod.POST, "/compras/*/compensar").hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.POST, "/entradas/emitir").hasAuthority("SERVICE_compras")
                        .pathMatchers(HttpMethod.POST, "/entradas/validar").hasRole("PERSONAL_ACCESO")
                        .pathMatchers(HttpMethod.PUT, "/entradas/accesos/eventos/*/personal/*").hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.DELETE, "/entradas/accesos/eventos/*/personal/*").hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.GET, "/notificaciones/*").hasAuthority("USER")
                        .pathMatchers(HttpMethod.POST, "/entradas/evento/*/invalidar")
                        .hasAnyAuthority("SERVICE_eventos", "SERVICE_compras")
                        .pathMatchers(HttpMethod.GET, "/entradas/*", "/entradas/cliente/*")
                        .hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR", "SERVICE_compras")
                        .pathMatchers(HttpMethod.POST, "/devoluciones").hasRole("CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/devoluciones/pendientes").hasRole("ADMINISTRADOR")
                        .pathMatchers(HttpMethod.PATCH, "/devoluciones/*/aprobar", "/devoluciones/*/rechazar")
                        .hasRole("ADMINISTRADOR")
                        .pathMatchers(
                                HttpMethod.POST,
                                "/filas/*/ingresar")
                        .hasRole("CLIENTE")
                        .pathMatchers(
                                HttpMethod.GET,
                                "/filas/*/clientes/*")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .pathMatchers(
                                HttpMethod.POST,
                                "/filas/*/habilitar")
                        .hasRole("ADMINISTRADOR")
                        .pathMatchers(
                                HttpMethod.GET,
                                "/filas/*/clientes/*/validar")
                        .hasAuthority("SERVICE_reservas")
                        .pathMatchers(
                                HttpMethod.POST,
                                "/filas/*/clientes/*/finalizar")
                        .hasAuthority("SERVICE_reservas")
                        .pathMatchers(
                                "/api/preferencias/**",
                                "/api/recomendaciones/**")
                        .hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .anyExchange().denyAll())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(
                        new ReactiveJwtAuthenticationConverterAdapter(JwtSupport.authenticationConverter()))))
                .build();
    }
}
