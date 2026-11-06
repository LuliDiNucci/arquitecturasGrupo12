
package com.ticketya.compras.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.ticketya.compras.dtos.ReservaDTO;

@FeignClient(name = "reservasClient", url = "${msvc.reservas.url}")
public interface ReservaClient {

    // Consultar una reserva antes de iniciar la compra
    @GetMapping("/reservas/{id}")
    ReservaDTO obtenerReserva(@PathVariable("id") Long id);

    // Confirmar cuando el pago sea aprobado
    @PostMapping("/reservas/{id}/confirmar")
    ReservaDTO confirmarReserva(@PathVariable("id") Long id);

    // Liberar cuando el pago sea rechazado
    @PostMapping("/reservas/{id}/liberar")
    ReservaDTO liberarReserva(@PathVariable("id") Long id);
}
