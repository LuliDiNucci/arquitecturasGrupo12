package com.ticketya.entradas.dtos;

import java.time.LocalDateTime;

public record EntradaResponseDTO(
    Long id,
    Long compraId,
    Long reservaId,
    Long clienteId,
    Long eventoId,
    Long sectorId,
    Long asientoId,
    String codigoQr,
    String estado,
    LocalDateTime fechaEmision,
    LocalDateTime fechaUso
) {}