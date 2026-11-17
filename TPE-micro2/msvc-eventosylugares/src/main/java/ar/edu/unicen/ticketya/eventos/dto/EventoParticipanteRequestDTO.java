package ar.edu.unicen.ticketya.eventos.dto;

import jakarta.validation.constraints.NotNull;

/** Asociacion de un participante a un evento, para una futura API de vinculos individuales. */

public class EventoParticipanteRequestDTO {
    @NotNull
    private Long eventoId;
    @NotNull
    private Long participanteId;

    public EventoParticipanteRequestDTO() {
    }

    public EventoParticipanteRequestDTO(Long eventoId, Long participanteId) {
        this.eventoId = eventoId;
        this.participanteId = participanteId;
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
