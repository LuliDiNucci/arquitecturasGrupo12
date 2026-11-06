package com.ticketya.reservas.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventoDTO {

    private Long id;
    private String nombre;
    private LocalDateTime fechaHora;
    private String estado;
    private Long lugarId;
    private LugarDTO lugar;

    public EventoDTO() {
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getLugarId() {
        if (lugarId != null) {
            return lugarId;
        }

        if (lugar != null) {
            return lugar.getId();
        }

        return null;
    }

    public void setLugarId(Long lugarId) {
        this.lugarId = lugarId;
    }

    public LugarDTO getLugar() {
        return lugar;
    }

    public void setLugar(LugarDTO lugar) {
        this.lugar = lugar;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LugarDTO {

        private Long id;

        public LugarDTO() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}