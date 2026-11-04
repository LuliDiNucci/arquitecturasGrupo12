
package com.ticketya.reservas.dtos;

import com.ticketya.reservas.enums.EstadoReserva;
import java.time.LocalDateTime;
import java.util.List;

public class ReservaDTO {

    private Long id;
    private Long clienteId;
    private Long eventoId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaVencimiento;
    private EstadoReserva estado;
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
