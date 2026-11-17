package com.ticketya.reservas.dtos;

public class SectorDTO {

    private Long id;
    private String nombre;
    private Boolean numerado;
    private Integer capacidad;
    private Long lugarId;

    public SectorDTO() {
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

    public Boolean getNumerado() {
        return numerado;
    }

    public void setNumerado(Boolean numerado) {
        this.numerado = numerado;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Long getLugarId() {
        return lugarId;
    }

    public void setLugarId(Long lugarId) {
        this.lugarId = lugarId;
    }
}