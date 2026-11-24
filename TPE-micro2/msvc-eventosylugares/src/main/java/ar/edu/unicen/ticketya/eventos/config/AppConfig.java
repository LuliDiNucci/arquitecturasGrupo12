package ar.edu.unicen.ticketya.eventos.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Argentina/Buenos_Aires"));
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("TicketYa - Eventos y Lugares")
                .description("Gestión de eventos, lugares, sectores, asientos, organizadores, participantes, precios y reportes de TicketYa.")
                .version("1.0.0"))
                .addServersItem(new Server().url("/"));
    }
}
