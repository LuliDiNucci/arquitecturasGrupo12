
package com.ticketya.compras.dtos;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una reserva obtenida desde el microservicio Reservas")
public class ReservaDTO {

    @Schema(description = "Identificador único de la reserva", example = "9")
    private Long id;

    @Schema(description = "ID del cliente que realizó la reserva", example = "200")
    private Long clienteId;

    @Schema(description = "ID del evento al que pertenece la reserva", example = "21")
    private Long eventoId;

    @Schema(description = "Fecha y hora en que se creó la reserva", example = "2026-11-05T18:34:09")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha y hora en que vence la reserva temporal", example = "2026-11-05T18:44:09")
    private LocalDateTime fechaVencimiento;

    @Schema(description = "Estado actual de la reserva", example = "ACTIVA", allowableValues = {
            "ACTIVA",
            "CONFIRMADA",
            "VENCIDA",
            "LIBERADA",
            "CANCELADA"
    })
    private String estado;

    @Schema(description = "Lista de sectores, cantidades y asientos asociados a la reserva")
    private List<DetalleReservaDTO> detalles;

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<DetalleReservaDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleReservaDTO> detalles) {
        this.detalles = detalles;
    }
}
