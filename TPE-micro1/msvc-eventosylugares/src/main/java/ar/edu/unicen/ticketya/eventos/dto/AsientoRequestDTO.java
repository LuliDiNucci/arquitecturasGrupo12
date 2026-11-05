package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

public class AsientoRequestDTO {
    @NotBlank @Size(max=20)
    @Schema(example = "A")
    private String fila;
    @Min(1)
    @Schema(example = "1")
    private int numero;

    public AsientoRequestDTO() {
    }

    public AsientoRequestDTO(String fila, int numero) {
        this.fila = fila;
        this.numero = numero;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
}
