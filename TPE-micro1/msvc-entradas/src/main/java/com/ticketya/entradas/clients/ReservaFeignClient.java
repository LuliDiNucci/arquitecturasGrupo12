package com.ticketya.entradas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "msvc-reservas")
public interface ReservaFeignClient {

    @PostMapping("/reservas/{id}/liberar")
    void liberarReserva(@PathVariable("id") Long id);
}