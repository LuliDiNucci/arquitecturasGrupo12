
package com.ticketya.reservas.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.ticketya.reservas.enums.EstadoReserva;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una reserva y su estado actual")
public class ReservaDTO {

    @Schema(description = "Identificador de la reserva", example = "1")
    private Long id;

    @Schema(description = "ID del cliente que realizó la reserva", example = "10")
    private Long clienteId;

    @Schema(description = "ID del evento reservado", example = "21")
    private Long eventoId;

    @Schema(description = "Fecha y hora de creación de la reserva", example = "2026-11-05T15:30:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha y hora de vencimiento de la reserva temporal", example = "2026-11-05T15:40:00")
    private LocalDateTime fechaVencimiento;

    @Schema(description = "Estado actual de la reserva", example = "ACTIVA")
    private EstadoReserva estado;

    @Schema(description = "Sectores, cantidades y asientos asociados a la reserva")
    private List<DetalleReservaRespuestaDTO> detalles;

    public ReservaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public List<DetalleReservaRespuestaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(
            List<DetalleReservaRespuestaDTO> detalles) {
        this.detalles = detalles;
    }
}
