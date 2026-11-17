
package com.ticketya.entradas.dtos;

public class CompraDTO {

    private Long id;
    private Long clienteId;
    private Long reservaId;
    private String estado;
    private Boolean pagoAprobado;

    public CompraDTO() {
    }

    public CompraDTO(Long id, Long clienteId, Long reservaId,
                     String estado, Boolean pagoAprobado) {
        this.id = id;
        this.clienteId = clienteId;
        this.reservaId = reservaId;
        this.estado = estado;
        this.pagoAprobado = pagoAprobado;
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

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getPagoAprobado() {
        return pagoAprobado;
    }

    public void setPagoAprobado(Boolean pagoAprobado) {
        this.pagoAprobado = pagoAprobado;
    }
}

