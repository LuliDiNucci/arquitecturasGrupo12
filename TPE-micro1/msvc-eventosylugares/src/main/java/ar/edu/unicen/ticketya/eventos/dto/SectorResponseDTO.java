package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class SectorResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long lugarId;
    @Schema(example = "Platea")
    private String nombre;
    @Schema(example = "true")
    private boolean numerado;
    @Schema(example = "2")
    private int capacidad;
    @Schema(example = "2")
    private long asientosCargados;

    public SectorResponseDTO() {
    }

    public SectorResponseDTO(Long id, Long lugarId, String nombre, boolean numerado, int capacidad, long asientosCargados) {
        this.id = id;
        this.lugarId = lugarId;
        this.nombre = nombre;
        this.numerado = numerado;
        this.capacidad = capacidad;
        this.asientosCargados = asientosCargados;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLugarId() {
        return lugarId;
    }

    public void setLugarId(Long lugarId) {
        this.lugarId = lugarId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isNumerado() {
        return numerado;
    }

    public void setNumerado(boolean numerado) {
        this.numerado = numerado;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public long getAsientosCargados() {
        return asientosCargados;
    }

    public void setAsientosCargados(long asientosCargados) {
        this.asientosCargados = asientosCargados;
    }
}
