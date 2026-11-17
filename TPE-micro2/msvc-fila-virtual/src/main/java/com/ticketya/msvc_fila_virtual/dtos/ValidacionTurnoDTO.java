
package com.ticketya.msvc_fila_virtual.dtos;

import java.time.LocalDateTime;

import com.ticketya.msvc_fila_virtual.entities.EstadoFila;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de la validación de un turno de fila virtual")
public record ValidacionTurnoDTO(

        @Schema(description = "Identificador del evento", example = "1") Long eventoId,

        @Schema(description = "Identificador del cliente", example = "10") Long clienteId,

        @Schema(description = "Indica si el cliente tiene un turno vigente") boolean autorizado,

        @Schema(description = "Estado actual del cliente", example = "HABILITADO") EstadoFila estado,

        @Schema(description = "Vencimiento de la autorización") LocalDateTime fechaVencimiento

) {
}
