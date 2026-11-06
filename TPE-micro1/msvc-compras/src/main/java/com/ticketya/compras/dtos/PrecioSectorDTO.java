
package com.ticketya.compras.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Precio de un sector para un evento, " +
        "obtenido desde el microservicio Eventos y Lugares")
public class PrecioSectorDTO {

    @Schema(description = "ID del evento para el que se define el precio", example = "21")
    private Long eventoId;

    @Schema(description = "ID del sector al que corresponde el precio", example = "5")
    private Long sectorId;

    @Schema(description = "Precio unitario de una entrada en ese sector. " +
            "Compras conserva este valor al iniciar la compra.", example = "15000.00", minimum = "0")
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
