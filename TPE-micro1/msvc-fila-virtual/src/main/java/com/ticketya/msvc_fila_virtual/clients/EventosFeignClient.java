
package com.ticketya.msvc_fila_virtual.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ticketya.msvc_fila_virtual.dtos.EventoDTO;

@FeignClient(
        name = "eventosClient",
        url = "${msvc.eventos.url}"
)
public interface EventosFeignClient {

    @GetMapping("/eventos/{id}")
    EventoDTO obtenerEvento(@PathVariable("id") Long id);
}
