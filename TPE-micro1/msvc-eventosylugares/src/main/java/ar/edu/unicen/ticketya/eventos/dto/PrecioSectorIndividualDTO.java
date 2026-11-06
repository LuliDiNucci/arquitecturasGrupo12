
package ar.edu.unicen.ticketya.eventos.dto;

import java.math.BigDecimal;

public class PrecioSectorIndividualDTO {

    private Long eventoId;
    private Long sectorId;
    private BigDecimal precio;

    public PrecioSectorIndividualDTO() {
    }

    public PrecioSectorIndividualDTO(
            Long eventoId,
            Long sectorId,
            BigDecimal precio) {

        this.eventoId = eventoId;
        this.sectorId = sectorId;
        this.precio = precio;
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
