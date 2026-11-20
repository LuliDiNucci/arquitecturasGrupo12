package com.ticketya.recomendaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import feign.RequestInterceptor;

public class CompraFeignConfig {

    @Bean
    public RequestInterceptor reenviarTokenCliente() {

        return template -> {

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof Jwt jwt
                    && "user".equals(jwt.getClaimAsString("token_use"))) {
                template.removeHeader("Authorization");
                template.header("Authorization", "Bearer " + jwt.getTokenValue());
            }
        };
    }
}
