
package com.ticketya.recomendaciones.controllers;

import com.ticketya.recomendaciones.dtos.EventoRecomendadoDTO;
import com.ticketya.recomendaciones.services.RecomendacionesService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Recomendaciones", description = "Consulta de eventos recomendados para un cliente")
@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionesController {

    private final RecomendacionesService service;

    public RecomendacionesController(RecomendacionesService service) {
        this.service = service;
    }

    @GetMapping("/{clienteId}")
    public List<EventoRecomendadoDTO> obtenerRecomendaciones(
            @PathVariable Long clienteId) {

        return service.obtenerRecomendaciones(clienteId);
    }
}
