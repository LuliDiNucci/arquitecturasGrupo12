package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import ar.edu.unicen.ticketya.eventos.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class EventoRequestDTO {
    @NotBlank @Size(max=180)
    @Schema(example = "Recital de prueba")
    private String nombre;
    @NotBlank @Size(max=4000)
    @Schema(example = "Evento para probar TicketYa")
    private String descripcion;
    @NotNull
    @Schema(example = "RECITAL")
    private TipoEvento tipo;
    @NotNull
    @Schema(example = "1")
    private Long organizadorId;
    @NotNull
    @Schema(example = "1")
    private Long lugarId;
    @NotNull
    @Schema(example = "2027-12-20T21:00:00")
    private LocalDateTime fechaHora;
    @NotEmpty
    @Schema(example = "[1,2]")
    private List<@NotNull Long> participantesIds;
    @Schema(example = "false")
    private boolean altaDemanda;

    public EventoRequestDTO() {
    }

    public EventoRequestDTO(String nombre, String descripcion, TipoEvento tipo, Long organizadorId, Long lugarId, LocalDateTime fechaHora, List<@NotNull Long> participantesIds, boolean altaDemanda) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.organizadorId = organizadorId;
        this.lugarId = lugarId;
        this.fechaHora = fechaHora;
        this.participantesIds = participantesIds;
        this.altaDemanda = altaDemanda;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public Long getOrganizadorId() {
        return organizadorId;
    }

    public void setOrganizadorId(Long organizadorId) {
        this.organizadorId = organizadorId;
    }

    public Long getLugarId() {
        return lugarId;
    }

    public void setLugarId(Long lugarId) {
        this.lugarId = lugarId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public List<@NotNull Long> getParticipantesIds() {
        return participantesIds;
    }

    public void setParticipantesIds(List<@NotNull Long> participantesIds) {
        this.participantesIds = participantesIds;
    }

    public boolean isAltaDemanda() {
        return altaDemanda;
    }

    public void setAltaDemanda(boolean altaDemanda) {
        this.altaDemanda = altaDemanda;
    }
}
