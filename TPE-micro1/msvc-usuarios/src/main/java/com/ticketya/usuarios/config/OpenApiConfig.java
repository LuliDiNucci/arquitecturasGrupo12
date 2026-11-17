package com.ticketya.usuarios.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean org.springdoc.core.customizers.OpenApiCustomizer errorDocumentation() {
        return api -> {
            var error = new io.swagger.v3.oas.models.media.ObjectSchema()
                .addProperty("timestamp", new io.swagger.v3.oas.models.media.StringSchema().format("date-time"))
                .addProperty("status", new io.swagger.v3.oas.models.media.IntegerSchema())
                .addProperty("error", new io.swagger.v3.oas.models.media.StringSchema())
                .addProperty("path", new io.swagger.v3.oas.models.media.StringSchema())
                .addProperty("detalles", new io.swagger.v3.oas.models.media.ArraySchema()
                    .items(new io.swagger.v3.oas.models.media.StringSchema()));
            api.getComponents().addSchemas("ErrorResponse", error);
            api.getPaths().values().forEach(path -> path.readOperations().forEach(operation ->
                operation.getResponses().forEach((code, response) -> {
                    if (java.util.Set.of("400", "401", "403", "404", "409", "429").contains(code))
                        response.setContent(new io.swagger.v3.oas.models.media.Content().addMediaType("application/json",
                            new io.swagger.v3.oas.models.media.MediaType().schema(
                                new io.swagger.v3.oas.models.media.Schema<>().$ref("#/components/schemas/ErrorResponse"))));
                })));
        };
    }
    @Bean OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("TicketYa - Usuarios").version("2.0.0")
            .description("Registro, login y roles. Los IDs de usuario identifican a los clientes de los otros servicios."))
            .components(new Components()
                .addSecuritySchemes("bearerJwt", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                .addSecuritySchemes("serviceBasic", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
    }
}
