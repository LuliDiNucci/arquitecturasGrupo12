package com.ticketya.notificaciones.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public record ArtistaFavoritoRequestDTO(
        @NotNull @Valid DestinatarioDTO destinatario,
        @NotNull @Positive @Schema(example = "45") Long eventoId,
        @NotBlank @Size(max = 180) @Schema(example = "Recital en Tandil") String nombreEvento,
        @NotNull @Schema(example = "2026-12-05T21:00:00", description = "Fecha y hora local del evento")
        LocalDateTime fechaHora,
        @NotBlank @Size(max = 100) @Schema(example = "Tandil") String ciudadEvento,
        @NotNull @Positive @Schema(example = "8") Long artistaId,
        @NotBlank @Size(max = 180) @Schema(example = "Los Viajeros") String nombreArtista,
        @NotBlank @Size(max = 100) @Schema(example = "Tandil") String ciudadCliente,
        @NotNull @Schema(example = "[8, 15]") Set<@NotNull @Positive Long> artistasFavoritosIds
) {}
