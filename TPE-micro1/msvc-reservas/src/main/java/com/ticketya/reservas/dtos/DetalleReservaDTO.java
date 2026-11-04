package com.ticketya.reservas.dtos;

import java.util.List;

public class DetalleReservaDTO {

    private Long sectorId;
    private Integer cantidad;
    private List<Long> asientoIds;

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

}