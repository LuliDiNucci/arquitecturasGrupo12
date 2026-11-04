
package com.ticketya.reservas.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.reservas.dtos.CrearReservaDTO;
import com.ticketya.reservas.dtos.DevolverEntradaDTO;
import com.ticketya.reservas.dtos.ReservaDTO;
import com.ticketya.reservas.services.ReservaService;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // CREAR RESERVA
    @PostMapping
    public ResponseEntity<ReservaDTO> crearReserva(
            @RequestBody CrearReservaDTO solicitud) {

        ReservaDTO reserva = reservaService.crearReserva(solicitud);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reserva);
    }

    // OBTENER RESERVA
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> obtenerReserva(
            @PathVariable Long id) {

        ReservaDTO reserva = reservaService.obtenerReserva(id);

        return ResponseEntity.ok(reserva);
    }

    // CONFIRMAR RESERVA
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<ReservaDTO> confirmarReserva(
            @PathVariable Long id) {

        ReservaDTO reserva = reservaService.confirmarReserva(id);

        return ResponseEntity.ok(reserva);
    }

    // LIBERAR RESERVA
    @PostMapping("/{id}/liberar")
    public ResponseEntity<ReservaDTO> liberarReserva(
            @PathVariable Long id) {

        ReservaDTO reserva = reservaService.liberarReserva(id);

        return ResponseEntity.ok(reserva);
    }

    // CANCELAR RESERVAS POR EVENTO
    @PostMapping("/evento/{eventoId}/cancelar")
    public ResponseEntity<Void> cancelarReservasEvento(
            @PathVariable Long eventoId) {

        reservaService.cancelarReservasEvento(eventoId);

        return ResponseEntity.noContent().build();
    }

    // DEVOLVER ENTRADA
    @PostMapping("/{id}/devolver-entrada")
    public ResponseEntity<ReservaDTO> devolverEntrada(
            @PathVariable Long id,
            @RequestBody DevolverEntradaDTO solicitud) {

        ReservaDTO reserva = reservaService.devolverEntrada(id, solicitud);

        return ResponseEntity.ok(reserva);
    }

    @PostMapping("/evento/{eventoId}/habilitar")
    public ResponseEntity<Void> habilitarEvento(
            @PathVariable Long eventoId) {

        reservaService.habilitarEvento(eventoId);

        return ResponseEntity.noContent().build();
    }

}
