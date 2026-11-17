
package com.ticketya.msvc_fila_virtual.dtos;

import java.time.LocalDateTime;

import com.ticketya.msvc_fila_virtual.entities.ClienteEnFila;
import com.ticketya.msvc_fila_virtual.entities.EstadoFila;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de un cliente en la fila virtual")
public record ClienteEnFilaDTO(

        @Schema(description = "ID del registro", example = "1") Long id,

        @Schema(description = "ID del evento", example = "1") Long eventoId,

        @Schema(description = "ID del cliente", example = "10") Long clienteId,

        @Schema(description = "Fecha de ingreso a la fila") LocalDateTime fechaIngreso,

        @Schema(description = "Estado actual", example = "EN_ESPERA") EstadoFila estado,

        @Schema(description = "Posición entre los clientes en espera", example = "3", nullable = true) Long posicion,

        @Schema(description = "Fecha de habilitación del turno", nullable = true) LocalDateTime fechaHabilitacion,

        @Schema(description = "Fecha de vencimiento del turno", nullable = true) LocalDateTime fechaVencimiento

) {

    public static ClienteEnFilaDTO desdeEntidad(
            ClienteEnFila cliente,
            Long posicion) {

        return new ClienteEnFilaDTO(
                cliente.getId(),
                cliente.getEventoId(),
                cliente.getClienteId(),
                cliente.getFechaIngreso(),
                cliente.getEstado(),
                posicion,
                cliente.getFechaHabilitacion(),
                cliente.getFechaVencimiento());
    }
}
