package com.ticketya.compras.security;

import feign.RequestInterceptor;
import org.springframework.context.annotation.*;

@Configuration
public class FeignSecurityConfig {
    @Bean RequestInterceptor bearerForwarding(InternalCredentials credentials) {
        return template -> {
            template.removeHeader("Authorization");
            template.header("Authorization", credentials.authorization(template.method()));
        };
    }
}
