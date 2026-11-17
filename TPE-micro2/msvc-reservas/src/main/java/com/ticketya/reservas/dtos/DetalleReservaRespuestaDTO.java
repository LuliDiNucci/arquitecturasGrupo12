
package com.ticketya.reservas.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un sector incluido en una reserva")
public class DetalleReservaRespuestaDTO {

    @Schema(description = "Identificador del detalle de reserva", example = "1")
    private Long id;

    @Schema(description = "ID del sector reservado", example = "5")
    private Long sectorId;

    @Schema(description = "Cantidad original de entradas reservadas", example = "2")
    private Integer cantidad;

    @Schema(description = "Asientos que continúan ocupados por la reserva", example = "[25, 26]")
    private List<Long> asientoIds;

    @Schema(description = "Cantidad de entradas individuales ya liberadas", example = "0")
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
