
package com.ticketya.reservas.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un sector solicitado en la reserva")
public class DetalleReservaDTO {

    @Schema(description = "ID del sector del evento", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sectorId;

    @Schema(description = "Cantidad de entradas solicitadas para el sector", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidad;

    @Schema(description = "IDs de los asientos elegidos. " +
            "Obligatorios para sectores numerados. " +
            "Para sectores no numerados se envía una lista vacía.", example = "[25, 26]")
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
