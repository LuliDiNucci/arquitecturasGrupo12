package com.ticketya.notificaciones;

import com.ticketya.notificaciones.config.NotificacionesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(NotificacionesProperties.class)
public class MsvcNotificacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsvcNotificacionesApplication.class, args);
    }
}
