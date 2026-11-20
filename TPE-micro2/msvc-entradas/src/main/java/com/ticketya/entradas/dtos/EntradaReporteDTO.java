
package com.ticketya.entradas.dtos;

public record EntradaReporteDTO(
        Long id,
        Long compraId,
        Long eventoId,
        Long sectorId,
        Long asientoId,
        String estado
) {
}
