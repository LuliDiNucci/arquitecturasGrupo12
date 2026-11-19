package com.ticketya.reservas.dtos;

import java.time.LocalDateTime;

public record ValidacionTurnoDTO(

        Long eventoId,

        Long clienteId,

        boolean autorizado,

        String estado,

        LocalDateTime fechaVencimiento

) {
}