package com.ticketya.recomendaciones.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ParticipanteDTO(
        Long id,
        String nombre,
        String tipo) {
}