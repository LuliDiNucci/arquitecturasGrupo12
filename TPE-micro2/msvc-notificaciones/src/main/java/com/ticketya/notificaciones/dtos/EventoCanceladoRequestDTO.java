package com.ticketya.notificaciones.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EventoCanceladoRequestDTO(
        @NotNull @Valid DestinatarioDTO destinatario,
        @NotNull @Positive @Schema(example = "45") Long eventoId,
        @NotBlank @Size(max = 180) @Schema(example = "Recital en Tandil") String nombreEvento
) {}
