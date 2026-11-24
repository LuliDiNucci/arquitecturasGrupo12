package com.ticketya.entradas.security;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiSecurityConfig {
    @Bean
    OpenApiCustomizer bearerDocumentation() {
        return api -> {
            api.setServers(java.util.List.of(new Server().url("/")));
            if (api.getComponents() == null)
                api.setComponents(new Components());
            api.getComponents().addSecuritySchemes("bearerJwt",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));
            // Los requisitos exactos por método figuran en la matriz de permisos.
            api.addSecurityItem(new SecurityRequirement().addList("bearerJwt"));
            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                boolean publicRead = false;
                if (publicRead)
                    operation.setSecurity(java.util.List.of());
                else {
                    operation.getResponses().addApiResponse("401", new io.swagger.v3.oas.models.responses.ApiResponse()
                            .description("JWT ausente, inválido o vencido"));
                    operation.getResponses().addApiResponse("403", new io.swagger.v3.oas.models.responses.ApiResponse()
                            .description("Rol o titularidad insuficiente"));
                }
            }));
        };
    }
}
