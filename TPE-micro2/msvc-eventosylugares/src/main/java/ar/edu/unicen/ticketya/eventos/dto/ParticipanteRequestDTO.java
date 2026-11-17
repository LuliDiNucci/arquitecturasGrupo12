package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import ar.edu.unicen.ticketya.eventos.model.*;
import jakarta.validation.constraints.*;

public class ParticipanteRequestDTO {
    @NotBlank @Size(max=150)
    @Schema(example = "Banda Ejemplo")
    private String nombre;
    @NotNull
    @Schema(example = "ARTISTA")
    private TipoParticipante tipo;

    public ParticipanteRequestDTO() {
    }

    public ParticipanteRequestDTO(String nombre, TipoParticipante tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoParticipante getTipo() {
        return tipo;
    }

    public void setTipo(TipoParticipante tipo) {
        this.tipo = tipo;
    }
}
