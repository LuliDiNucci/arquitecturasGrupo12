
package com.ticketya.reservas.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para crear una reserva temporal")
public class CrearReservaDTO {

    @Schema(description = "ID del cliente que realiza la reserva", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long clienteId;

    @Schema(description = "ID del evento al que pertenece la reserva", example = "21", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long eventoId;

    @Schema(description = "Clave única para evitar reservas duplicadas ante reintentos", example = "RESERVA-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String claveIdempotencia;

    @Schema(description = "Lista de sectores, cantidades y asientos solicitados", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DetalleReservaDTO> detalles;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public void setClaveIdempotencia(String claveIdempotencia) {
        this.claveIdempotencia = claveIdempotencia;
    }

    public List<DetalleReservaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleReservaDTO> detalles) {
        this.detalles = detalles;
    }
}
