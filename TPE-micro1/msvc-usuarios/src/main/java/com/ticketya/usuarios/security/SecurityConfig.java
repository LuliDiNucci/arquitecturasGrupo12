package com.ticketya.usuarios.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    JwtDecoder jwtDecoder(@Value("${ticketya.jwt.public-key}") Resource key,
            @Value("${ticketya.jwt.issuer}") String issuer, @Value("${ticketya.jwt.audience}") String audience) {
        return JwtSupport.decoder(key, issuer, audience);
    }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiSecurityErrors errors) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/service-token").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/usuarios/**").hasAuthority("USER")
                .anyRequest().denyAll())
            .exceptionHandling(e -> e.authenticationEntryPoint(errors).accessDeniedHandler(errors))
            .oauth2ResourceServer(o -> o.authenticationEntryPoint(errors).accessDeniedHandler(errors)
                .jwt(j -> j.jwtAuthenticationConverter(JwtSupport.authenticationConverter())))
            .build();
    }
}
