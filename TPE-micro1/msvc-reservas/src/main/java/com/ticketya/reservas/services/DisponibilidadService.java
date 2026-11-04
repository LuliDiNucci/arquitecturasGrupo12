package com.ticketya.reservas.services;

import com.ticketya.reservas.dtos.DisponibilidadDTO;

public interface DisponibilidadService {

    DisponibilidadDTO consultarDisponibilidad(
            Long eventoId,
            Long sectorId
    );
}