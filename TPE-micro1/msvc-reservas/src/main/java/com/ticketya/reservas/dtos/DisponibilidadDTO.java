
package com.ticketya.reservas.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Disponibilidad actual de lugares y asientos de un sector")
public class DisponibilidadDTO {

    @Schema(description = "ID del evento consultado", example = "21")
    private Long eventoId;

    @Schema(description = "ID del sector consultado", example = "5")
    private Long sectorId;

    @Schema(description = "Cantidad de lugares disponibles en el sector", example = "3")
    private Integer lugaresDisponibles;

    @Schema(description = "IDs de los asientos disponibles en sectores numerados. " +
            "En sectores no numerados se devuelve una lista vacía.", example = "[25, 26, 27]")
    private List<Long> asientosDisponibles;

    @Schema(description = "IDs de los asientos ocupados por reservas activas vigentes o confirmadas", example = "[25, 26]")
    private List<Long> asientosOcupados;

    public DisponibilidadDTO() {
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

    public Integer getLugaresDisponibles() {
        return lugaresDisponibles;
    }

    public void setLugaresDisponibles(Integer lugaresDisponibles) {
        this.lugaresDisponibles = lugaresDisponibles;
    }

    public List<Long> getAsientosDisponibles() {
        return asientosDisponibles;
    }

    public void setAsientosDisponibles(List<Long> asientosDisponibles) {
        this.asientosDisponibles = asientosDisponibles;
    }

    public List<Long> getAsientosOcupados() {
        return asientosOcupados;
    }

    public void setAsientosOcupados(List<Long> asientosOcupados) {
        this.asientosOcupados = asientosOcupados;
    }
}
