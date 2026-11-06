
package com.ticketya.compras.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ticketya.compras.dtos.EventoDTO;
import com.ticketya.compras.dtos.PrecioSectorDTO;

@FeignClient(name = "eventosClient", url = "${msvc.eventos.url}")
public interface EventoClient {

    @GetMapping("/eventos/{eventoId}/sectores/{sectorId}/precio")
    PrecioSectorDTO obtenerPrecioSector(
            @PathVariable("eventoId") Long eventoId,
            @PathVariable("sectorId") Long sectorId);

    @GetMapping("/eventos/{id}")
    EventoDTO obtenerEvento(
            @PathVariable("id") Long id);
}
