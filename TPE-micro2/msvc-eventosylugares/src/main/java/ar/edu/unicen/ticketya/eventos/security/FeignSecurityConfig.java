package ar.edu.unicen.ticketya.eventos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class FeignSecurityConfig {
    @Bean
    RequestInterceptor bearerForwarding(InternalCredentials credentials) {
        return template -> {
            template.removeHeader("Authorization");

            String ruta = template.path();

            boolean consultaInternaRecomendaciones = ruta.equals("/internal/clientes-interesados");

            boolean consultaContacto = ruta.matches("/usuarios/[0-9]+/contacto");

            if (consultaInternaRecomendaciones || consultaContacto) {
                template.header(
                        "Authorization",
                        credentials.serviceAuthorization());
            } else {
                template.header(
                        "Authorization",
                        credentials.authorization(template.method()));
            }
        };
    }
}
