package com.ticketya.notificaciones.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record EventoModificadoRequestDTO(
        @NotNull @Valid DestinatarioDTO destinatario,
        @NotNull @Positive @Schema(example = "45") Long eventoId,
        @NotNull @PositiveOrZero @Schema(example = "3", description = "Versión del evento tras el cambio") Long versionEvento,
        @NotBlank @Size(max = 180) @Schema(example = "Recital en Tandil") String nombreEvento,
        @NotBlank @Size(max = 1000) @Schema(example = "La apertura de puertas se pasa a las 20:30.") String detalleCambios
) {}
