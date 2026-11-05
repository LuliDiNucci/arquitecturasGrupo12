package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import ar.edu.unicen.ticketya.eventos.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class EventoResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Recital de prueba")
    private String nombre;
    @Schema(example = "Presentacion en vivo")
    private String descripcion;
    @Schema(example = "RECITAL")
    private TipoEvento tipo;
    @Schema(example = "PUBLICADO")
    private EstadoEvento estado;
    private OrganizadorResponseDTO organizador;
    private List<ParticipanteResponseDTO> participantes;
    @Schema(example = "2027-12-20T21:00:00")
    private LocalDateTime fechaHora;
    private LugarResponseDTO lugar;
    @Schema(example = "false")
    private boolean altaDemanda;
    private List<PrecioSectorResponseDTO> sectores;
    @Schema(example = "0")
    private Long version;

    public EventoResponseDTO() {
    }

    public EventoResponseDTO(Long id, String nombre, String descripcion, TipoEvento tipo, EstadoEvento estado, OrganizadorResponseDTO organizador, List<ParticipanteResponseDTO> participantes, LocalDateTime fechaHora, LugarResponseDTO lugar, boolean altaDemanda, List<PrecioSectorResponseDTO> sectores, Long version) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estado = estado;
        this.organizador = organizador;
        this.participantes = participantes;
        this.fechaHora = fechaHora;
        this.lugar = lugar;
        this.altaDemanda = altaDemanda;
        this.sectores = sectores;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public OrganizadorResponseDTO getOrganizador() {
        return organizador;
    }

    public void setOrganizador(OrganizadorResponseDTO organizador) {
        this.organizador = organizador;
    }

    public List<ParticipanteResponseDTO> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<ParticipanteResponseDTO> participantes) {
        this.participantes = participantes;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public LugarResponseDTO getLugar() {
        return lugar;
    }

    public void setLugar(LugarResponseDTO lugar) {
        this.lugar = lugar;
    }

    public boolean isAltaDemanda() {
        return altaDemanda;
    }

    public void setAltaDemanda(boolean altaDemanda) {
        this.altaDemanda = altaDemanda;
    }

    public List<PrecioSectorResponseDTO> getSectores() {
        return sectores;
    }

    public void setSectores(List<PrecioSectorResponseDTO> sectores) {
        this.sectores = sectores;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
