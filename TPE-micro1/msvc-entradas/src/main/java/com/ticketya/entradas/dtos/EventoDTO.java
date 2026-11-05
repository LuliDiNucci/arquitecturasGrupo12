package com.ticketya.entradas.dtos;

import java.time.LocalDateTime;

public record EventoDTO(
    Long id,
    LocalDateTime fechaHora,
    String estado
) {}