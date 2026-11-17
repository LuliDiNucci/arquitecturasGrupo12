
package com.ticketya.compras.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un sector y sus asientos asociados a una reserva")
public class DetalleReservaDTO {

    @Schema(description = "Identificador único del detalle de reserva", example = "1")
    private Long id;

    @Schema(description = "ID del sector al que pertenecen las entradas reservadas", example = "5")
    private Long sectorId;

    @Schema(description = "Cantidad de entradas reservadas en el sector", example = "2")
    private Integer cantidad;

    @Schema(description = "Cantidad de entradas que ya fueron liberadas", example = "0")
    private Integer cantidadLiberada;

    @Schema(description = "IDs de los asientos reservados que siguen ocupados. " +
            "En sectores no numerados se devuelve una lista vacía.", example = "[25, 26]")
    private List<Long> asientoIds;

    public DetalleReservaDTO() {
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
