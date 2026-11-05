
package com.ticketya.entradas.dtos;

import java.util.List;

public class ReservaDTO {

    private Long id;
    private Long clienteId;
    private Long eventoId;
    private String estado;
    private List<DetalleReservaDTO> detalles;

    public ReservaDTO() {
    }

    public ReservaDTO(Long id, Long clienteId, Long eventoId,
                      String estado, List<DetalleReservaDTO> detalles) {
        this.id = id;
        this.clienteId = clienteId;
        this.eventoId = eventoId;
        this.estado = estado;
        this.detalles = detalles;
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
