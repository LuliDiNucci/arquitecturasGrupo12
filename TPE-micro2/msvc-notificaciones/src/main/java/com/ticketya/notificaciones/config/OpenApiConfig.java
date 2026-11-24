package com.ticketya.notificaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificacionesOpenApi() {
        return new OpenAPI()
                .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("bearerJwt",
                        new io.swagger.v3.oas.models.security.SecurityScheme().type(
                                io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerJwt"))
                .info(new Info()
                .title("TicketYa - Notificaciones")
                .version("1.0")
                .description("API interna de solicitudes de correo y consulta del estado de envío."))
                .addServersItem(new Server().url("/"));
    }
}
