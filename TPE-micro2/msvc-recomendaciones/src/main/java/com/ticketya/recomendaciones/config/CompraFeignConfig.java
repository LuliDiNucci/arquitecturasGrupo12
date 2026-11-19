package com.ticketya.recomendaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;

public class CompraFeignConfig {

    @Bean
    public RequestInterceptor reenviarTokenCliente() {

        return template -> {

            ServletRequestAttributes atributos = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (atributos != null) {

                String authorization = atributos
                        .getRequest()
                        .getHeader("Authorization");

                if (authorization != null
                        && authorization.startsWith("Bearer ")) {

                    template.header("Authorization", authorization);
                }
            }
        };
    }
}