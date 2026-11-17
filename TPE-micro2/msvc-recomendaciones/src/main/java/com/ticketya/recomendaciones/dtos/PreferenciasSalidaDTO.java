
package com.ticketya.recomendaciones.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Información de las preferencias registradas de un cliente")
public class PreferenciasSalidaDTO {

    @Schema(description = "Identificador del cliente", example = "15")
    private Long clienteId;

    @Schema(description = "Tipos de eventos preferidos")
    private List<String> tiposEventos;

    @Schema(description = "Artistas favoritos")
    private List<String> artistasFavoritos;

    @Schema(description = "Ciudad de preferencia", example = "Tandil")
    private String ciudad;

    public PreferenciasSalidaDTO() {
    }

    public PreferenciasSalidaDTO(Long clienteId,
                                 List<String> tiposEventos,
                                 List<String> artistasFavoritos,
                                 String ciudad) {
        this.clienteId = clienteId;
        this.tiposEventos = tiposEventos;
        this.artistasFavoritos = artistasFavoritos;
        this.ciudad = ciudad;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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
