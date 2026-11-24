
package com.ticketya.reservas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI openAPI() {

                return new OpenAPI()
                                .info(new Info()
                                                .title("TicketYa - Reservas")
                                                .description(
                                                                "Gestión de disponibilidad, reservas temporales, confirmación, "
                                                                                +
                                                                                "liberación, cancelaciones, devoluciones e integración con Fila Virtual.")
                                                .version("1.0.0"))
                                .addServersItem(new Server().url("/"));
        }
}
