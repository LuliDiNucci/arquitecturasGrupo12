package com.ticketya.recomendaciones.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.recomendaciones.dtos.EventoRecomendadoDTO;
import com.ticketya.recomendaciones.services.RecomendacionesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @PreAuthorize("@clienteAccess.puedeAcceder(#clienteId)")
    @Operation(summary = "Obtener recomendaciones para un cliente", description = "Sugiere eventos publicados y futuros según los tipos de eventos, artistas favoritos y ciudad configurados por el cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recomendaciones obtenidas correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para consultar recomendaciones de otro cliente"),
            @ApiResponse(responseCode = "404", description = "El cliente no tiene preferencias registradas"),
            @ApiResponse(responseCode = "503", description = "El microservicio de Eventos no está disponible")
    })
    public List<EventoRecomendadoDTO> obtenerRecomendaciones(
            @PathVariable Long clienteId) {

        return service.obtenerRecomendaciones(clienteId);
    }
}