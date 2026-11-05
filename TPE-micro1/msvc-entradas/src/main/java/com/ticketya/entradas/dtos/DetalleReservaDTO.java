
package com.ticketya.entradas.dtos;

import java.util.List;

public class DetalleReservaDTO {

    private Long id;
    private Long sectorId;
    private Integer cantidad;
    private List<Long> asientoIds;
    private Integer cantidadLiberada;

    public DetalleReservaDTO() {
    }

    public DetalleReservaDTO(Long id, Long sectorId, Integer cantidad,
                             List<Long> asientoIds, Integer cantidadLiberada) {
        this.id = id;
        this.sectorId = sectorId;
        this.cantidad = cantidad;
        this.asientoIds = asientoIds;
        this.cantidadLiberada = cantidadLiberada;
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

    public List<Long> getAsientoIds() {
        return asientoIds;
    }

    public void setAsientoIds(List<Long> asientoIds) {
        this.asientoIds = asientoIds;
    }

    public Integer getCantidadLiberada() {
        return cantidadLiberada;
    }

    public void setCantidadLiberada(Integer cantidadLiberada) {
        this.cantidadLiberada = cantidadLiberada;
    }
}
