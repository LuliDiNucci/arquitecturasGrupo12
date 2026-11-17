
package com.ticketya.msvc_fila_virtual.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoDTO(
        Long id,
        String nombre,
        String estado,
        boolean altaDemanda) {
}
