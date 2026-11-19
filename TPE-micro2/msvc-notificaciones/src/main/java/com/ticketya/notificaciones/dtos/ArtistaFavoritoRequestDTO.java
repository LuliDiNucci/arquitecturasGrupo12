package com.ticketya.notificaciones.dtos;

import java.time.LocalDateTime;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ArtistaFavoritoRequestDTO(

                @NotNull @Valid DestinatarioDTO destinatario,

                @NotNull @Positive @Schema(example = "45") Long eventoId,

                @NotBlank @Size(max = 180) @Schema(example = "Recital en Tandil") String nombreEvento,

                @NotNull @Schema(example = "2027-02-20T21:00:00") LocalDateTime fechaHora,

                @NotBlank @Size(max = 100) @Schema(example = "Tandil") String ciudadEvento,

                @NotBlank @Size(max = 180) @Schema(example = "Banda de Prueba TicketYa") String nombreArtista,

                @NotBlank @Size(max = 100) @Schema(example = "Tandil") String ciudadCliente,

                @NotNull @Schema(description = "Nombres de los artistas favoritos del cliente", example = "[\"Banda de Prueba TicketYa\"]") Set<@NotBlank String> artistasFavoritos

) {
}