package com.ticketya.entradas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ticketya.entradas.dtos.DevolverEntradaDTO;

@FeignClient(name = "msvc-reservas", url = "${msvc.reservas.url}")
public interface ReservaFeignClient {

    @PostMapping("/reservas/{id}/liberar")
    void liberarReserva(@PathVariable("id") Long id);

    @PostMapping("/reservas/{id}/devolver-entrada")
    void devolverEntrada(
            @PathVariable("id") Long id,
            @RequestBody DevolverEntradaDTO solicitud);
}