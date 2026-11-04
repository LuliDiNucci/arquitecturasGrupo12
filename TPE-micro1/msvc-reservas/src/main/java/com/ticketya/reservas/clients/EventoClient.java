package com.ticketya.reservas.clients;

import com.ticketya.reservas.dtos.EventoDTO;
import com.ticketya.reservas.dtos.PrecioSectorDTO;
import com.ticketya.reservas.dtos.SectorDTO;
import com.ticketya.reservas.dtos.AsientoDTO;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "msvc-eventos-lugares", url = "${msvc.eventos.url}")
public interface EventoClient {

    // Obtener un evento por ID
    @GetMapping("/eventos/{id}")
    EventoDTO obtenerEvento(
            @PathVariable("id") Long id);

    // Obtener un sector por ID
    @GetMapping("/sectores/{id}")
    SectorDTO obtenerSector(
            @PathVariable("id") Long id);

    // Obtener un asiento por ID
    @GetMapping("/asientos/{id}")
    AsientoDTO obtenerAsiento(
            @PathVariable("id") Long id);

    @GetMapping("/sectores/{id}/asientos")
    List<AsientoDTO> obtenerAsientosPorSector(
            @PathVariable("id") Long sectorId);

    @GetMapping("/eventos/{eventoId}/sectores/{sectorId}/precio")
    PrecioSectorDTO obtenerPrecioSector(
            @PathVariable("eventoId") Long eventoId,
            @PathVariable("sectorId") Long sectorId);
}