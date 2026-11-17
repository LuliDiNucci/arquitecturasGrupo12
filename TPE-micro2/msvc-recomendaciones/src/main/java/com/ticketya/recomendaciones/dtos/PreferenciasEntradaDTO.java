
package com.ticketya.recomendaciones.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Datos para registrar o modificar las preferencias de un cliente")
public class PreferenciasEntradaDTO {

    @NotEmpty(message = "Debe indicar al menos un tipo de evento")
    @Schema(description = "Tipos de eventos preferidos", example = "[\"RECITAL\", \"TEATRO\"]")
    private List<@NotBlank String> tiposEventos;

    @NotNull(message = "La lista de artistas no puede ser nula")
    @Schema(description = "Artistas favoritos del cliente", example = "[\"Artista A\", \"Artista B\"]")
    private List<String> artistasFavoritos;

    @NotBlank(message = "La ciudad es obligatoria")
    @Schema(description = "Ciudad de preferencia", example = "Tandil")
    private String ciudad;

    public PreferenciasEntradaDTO() {
    }

    public PreferenciasEntradaDTO(List<String> tiposEventos,
                                  List<String> artistasFavoritos,
                                  String ciudad) {
        this.tiposEventos = tiposEventos;
        this.artistasFavoritos = artistasFavoritos;
        this.ciudad = ciudad;
    }

    public List<String> getTiposEventos() {
        return tiposEventos;
    }

    public void setTiposEventos(List<String> tiposEventos) {
        this.tiposEventos = tiposEventos;
    }

    public List<String> getArtistasFavoritos() {
        return artistasFavoritos;
    }

    public void setArtistasFavoritos(List<String> artistasFavoritos) {
        this.artistasFavoritos = artistasFavoritos;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
}
