package com.ticketya.reservas.security;

import com.ticketya.reservas.repositories.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component("reservationAccess")
public class ReservationAccess {
    private final ReservaRepository reservas;
    public ReservationAccess(ReservaRepository reservas) { this.reservas = reservas; }
    public boolean client(Long id) { return CurrentIdentity.owner(id); }
    public boolean reservation(Long id) {
        if (CurrentIdentity.admin() || CurrentIdentity.authority("SERVICE_compras") || CurrentIdentity.authority("SERVICE_entradas")) return true;
        return CurrentIdentity.owner(reservas.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso inexistente")).getClienteId());
    }
}
