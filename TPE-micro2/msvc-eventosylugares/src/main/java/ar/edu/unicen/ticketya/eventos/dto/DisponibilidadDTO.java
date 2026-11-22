package ar.edu.unicen.ticketya.eventos.dto;

import java.util.List;

public class DisponibilidadDTO {

    private Long eventoId;
    private Long sectorId;
    private Integer lugaresDisponibles;
    private List<Long> asientosDisponibles;
    private List<Long> asientosOcupados;

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