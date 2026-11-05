package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import ar.edu.unicen.ticketya.eventos.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventoResumenResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Recital de prueba")
    private String nombre;
    @Schema(example = "RECITAL")
    private TipoEvento tipo;
    @Schema(example = "PUBLICADO")
    private EstadoEvento estado;
    @Schema(example = "2027-12-20T21:00:00")
    private LocalDateTime fechaHora;
    @Schema(example = "Tandil")
    private String ciudad;
    @Schema(example = "Estadio Municipal")
    private String lugar;
    @Schema(example = "Producciones Sierra")
    private String organizador;
    @Schema(example = "35000.00")
    private BigDecimal precioDesde;
    @Schema(example = "false")
    private boolean altaDemanda;

    public EventoResumenResponseDTO() {
    }

    public EventoResumenResponseDTO(Long id, String nombre, TipoEvento tipo, EstadoEvento estado, LocalDateTime fechaHora, String ciudad, String lugar, String organizador, BigDecimal precioDesde, boolean altaDemanda) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaHora = fechaHora;
        this.ciudad = ciudad;
        this.lugar = lugar;
        this.organizador = organizador;
        this.precioDesde = precioDesde;
        this.altaDemanda = altaDemanda;
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getOrganizador() {
        return organizador;
    }

    public void setOrganizador(String organizador) {
        this.organizador = organizador;
    }

    public BigDecimal getPrecioDesde() {
        return precioDesde;
    }

    public void setPrecioDesde(BigDecimal precioDesde) {
        this.precioDesde = precioDesde;
    }

    public boolean isAltaDemanda() {
        return altaDemanda;
    }

    public void setAltaDemanda(boolean altaDemanda) {
        this.altaDemanda = altaDemanda;
    }
}
