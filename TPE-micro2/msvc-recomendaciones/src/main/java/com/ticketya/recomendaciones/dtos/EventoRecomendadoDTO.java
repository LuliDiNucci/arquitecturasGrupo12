package com.ticketya.recomendaciones.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información de un evento recomendado para un cliente")
public class EventoRecomendadoDTO {

    @Schema(description = "Identificador del evento", example = "25")
    private Long id;

    @Schema(description = "Nombre del evento", example = "Festival de Música")
    private String nombre;

    @Schema(description = "Tipo de evento", example = "RECITAL")
    private String tipo;

    @Schema(description = "Artista principal", example = "Artista A")
    private String artista;

    @Schema(description = "Ciudad donde se realiza", example = "Tandil")
    private String ciudad;

    @Schema(description = "Fecha y hora del evento",
            example = "2026-12-15T20:00:00")
    private LocalDateTime fechaHora;

    @Schema(description = "Precio de la entrada", example = "15000.0")
    private Double precio;

    public EventoRecomendadoDTO() {
    }

    public EventoRecomendadoDTO(Long id, String nombre, String tipo,
                                String artista, String ciudad,
                                LocalDateTime fechaHora, Double precio) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.artista = artista;
        this.ciudad = ciudad;
        this.fechaHora = fechaHora;
        this.precio = precio;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }
}
