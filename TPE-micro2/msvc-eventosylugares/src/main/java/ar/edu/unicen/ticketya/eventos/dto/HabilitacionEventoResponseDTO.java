package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import ar.edu.unicen.ticketya.eventos.model.*;
import java.time.LocalDateTime;

public class HabilitacionEventoResponseDTO {
    @Schema(example = "1")
    private Long eventoId;
    @Schema(example = "PUBLICADO")
    private EstadoEvento estado;
    @Schema(example = "true")
    private boolean habilitadoParaReservar;
    @Schema(example = "2027-12-20T21:00:00")
    private LocalDateTime fechaHora;

    public HabilitacionEventoResponseDTO() {
    }

    public HabilitacionEventoResponseDTO(Long eventoId, EstadoEvento estado, boolean habilitadoParaReservar, LocalDateTime fechaHora) {
        this.eventoId = eventoId;
        this.estado = estado;
        this.habilitadoParaReservar = habilitadoParaReservar;
        this.fechaHora = fechaHora;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public boolean isHabilitadoParaReservar() {
        return habilitadoParaReservar;
    }

    public void setHabilitadoParaReservar(boolean habilitadoParaReservar) {
        this.habilitadoParaReservar = habilitadoParaReservar;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
