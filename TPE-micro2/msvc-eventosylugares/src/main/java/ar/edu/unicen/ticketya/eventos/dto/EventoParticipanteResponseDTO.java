package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Representacion de la entidad asociativa EventoParticipante. */

public class EventoParticipanteResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long eventoId;
    @Schema(example = "1")
    private Long participanteId;

    public EventoParticipanteResponseDTO() {
    }

    public EventoParticipanteResponseDTO(Long id, Long eventoId, Long participanteId) {
        this.id = id;
        this.eventoId = eventoId;
        this.participanteId = participanteId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public Long getParticipanteId() {
        return participanteId;
    }

    public void setParticipanteId(Long participanteId) {
        this.participanteId = participanteId;
    }
}
