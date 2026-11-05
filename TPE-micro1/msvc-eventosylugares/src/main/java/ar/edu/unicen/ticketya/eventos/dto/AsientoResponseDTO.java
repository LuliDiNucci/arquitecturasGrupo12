package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AsientoResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "A")
    private String fila;
    @Schema(example = "1")
    private int numero;

    public AsientoResponseDTO() {
    }

    public AsientoResponseDTO(Long id, String fila, int numero) {
        this.id = id;
        this.fila = fila;
        this.numero = numero;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
