
package com.ticketya.reservas.dtos;

import java.util.List;

public class DetalleReservaRespuestaDTO {

    private Long id;
    private Long sectorId;
    private Integer cantidad;
    private List<Long> asientoIds;
    private Integer cantidadLiberada;

    public DetalleReservaRespuestaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidadLiberada() {
        return cantidadLiberada;
    }

    public void setCantidadLiberada(Integer cantidadLiberada) {
        this.cantidadLiberada = cantidadLiberada;
    }

    public List<Long> getAsientoIds() {
        return asientoIds;
    }

    public void setAsientoIds(List<Long> asientoIds) {
        this.asientoIds = asientoIds;
    }
}
