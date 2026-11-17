
package com.ticketya.reservas.services;

import com.ticketya.reservas.dtos.CrearReservaDTO;
import com.ticketya.reservas.dtos.DevolverEntradaDTO;
import com.ticketya.reservas.dtos.ReservaDTO;

public interface ReservaService {

    ReservaDTO crearReserva(CrearReservaDTO solicitud);

    ReservaDTO obtenerReserva(Long reservaId);

    ReservaDTO confirmarReserva(Long reservaId);

    ReservaDTO liberarReserva(Long reservaId);

    void cancelarReservasEvento(Long eventoId);

    ReservaDTO devolverEntrada(
            Long reservaId,
            DevolverEntradaDTO solicitud);

    void habilitarEvento(Long eventoId);
}
