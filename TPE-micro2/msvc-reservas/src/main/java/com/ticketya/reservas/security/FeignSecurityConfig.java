package com.ticketya.reservas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class FeignSecurityConfig {

    @Bean
    RequestInterceptor bearerForwarding(InternalCredentials credentials) {

        return template -> {

            template.removeHeader("Authorization");

            boolean esFilaVirtual = template.url().startsWith("/filas/");

            String metodoCredencial = esFilaVirtual
                    ? "POST"
                    : template.method();

            template.header(
                    "Authorization",
                    credentials.authorization(metodoCredencial));
        };
    }
}