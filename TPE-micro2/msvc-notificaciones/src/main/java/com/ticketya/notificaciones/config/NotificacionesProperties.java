package com.ticketya.notificaciones.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.notificaciones")
public record NotificacionesProperties(
        @NotBlank @Email String remitente,
        @Positive long intervaloMs,
        @Positive int maxIntentos,
        @Positive long reintentoSegundos
) {}
