
package com.ticketya.reservas.dtos;

import java.math.BigDecimal;

public class PrecioSectorDTO {

    private Long eventoId;
    private Long sectorId;
    private BigDecimal precio;

    public PrecioSectorDTO() {
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
