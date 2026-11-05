package com.ticketya.entradas.dtos;

public record EntradaRequestDTO(
    Long compraId,
    Long reservaId,
    Long clienteId,
    Long eventoId,
    Long sectorId,
    Long asientoId
) {}
