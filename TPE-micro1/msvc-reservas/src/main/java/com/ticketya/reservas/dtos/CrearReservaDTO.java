package com.ticketya.reservas.dtos;

import java.util.List;

public class CrearReservaDTO {

    private Long clienteId;
    private Long eventoId;
    private String claveIdempotencia;
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

    // Getters y setters
}