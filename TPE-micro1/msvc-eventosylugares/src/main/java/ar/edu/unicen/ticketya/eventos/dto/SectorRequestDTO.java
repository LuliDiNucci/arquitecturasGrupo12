package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

public class SectorRequestDTO {
    @NotBlank @Size(max=120)
    @Schema(example = "Platea")
    private String nombre;
    @Schema(example = "true")
    private boolean numerado;
    @Min(1)
    @Schema(example = "2")
    private int capacidad;

    public SectorRequestDTO() {
    }

    public SectorRequestDTO(String nombre, boolean numerado, int capacidad) {
        this.nombre = nombre;
        this.numerado = numerado;
        this.capacidad = capacidad;
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
}
