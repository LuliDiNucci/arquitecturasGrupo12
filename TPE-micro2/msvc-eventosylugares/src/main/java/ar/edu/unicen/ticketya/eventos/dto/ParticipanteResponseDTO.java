package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import ar.edu.unicen.ticketya.eventos.model.*;

public class ParticipanteResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Banda Ejemplo")
    private String nombre;
    @Schema(example = "ARTISTA")
    private TipoParticipante tipo;

    public ParticipanteResponseDTO() {
    }

    public ParticipanteResponseDTO(Long id, String nombre, TipoParticipante tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
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

    public TipoParticipante getTipo() {
        return tipo;
    }

    public void setTipo(TipoParticipante tipo) {
        this.tipo = tipo;
    }
}
