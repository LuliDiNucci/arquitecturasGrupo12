package com.ticketya.recomendaciones.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDetalleDTO(
        Long id,
        List<ParticipanteDTO> participantes) {
}