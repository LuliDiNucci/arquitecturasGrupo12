
package com.ticketya.recomendaciones.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "preferencias")
public class Preferencias {

    @Id
    private String id;

    private Long clienteId;

    private List<String> tiposEventos = new ArrayList<>();

    private List<String> artistasFavoritos = new ArrayList<>();

    private String ciudad;

    public Preferencias() {
    }

    public Preferencias(Long clienteId, List<String> tiposEventos,
                        List<String> artistasFavoritos, String ciudad) {
        this.clienteId = clienteId;
        this.tiposEventos = tiposEventos;
        this.artistasFavoritos = artistasFavoritos;
        this.ciudad = ciudad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
