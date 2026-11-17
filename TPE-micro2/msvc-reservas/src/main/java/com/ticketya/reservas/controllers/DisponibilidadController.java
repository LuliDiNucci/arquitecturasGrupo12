
package com.ticketya.reservas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.reservas.dtos.DisponibilidadDTO;
import com.ticketya.reservas.services.DisponibilidadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Disponibilidad", description = "Consulta de lugares y asientos disponibles para eventos")
@RestController
@RequestMapping("/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(
            DisponibilidadService disponibilidadService) {

        this.disponibilidadService = disponibilidadService;
    }

    @Operation(summary = "Consultar disponibilidad de un sector", description = "Consulta la cantidad de lugares disponibles "
            +
            "para un sector de un evento. " +
            "En sectores numerados devuelve los IDs de los " +
            "asientos libres. En sectores no numerados devuelve " +
            "la capacidad restante. " +
            "No realiza ninguna reserva. " +
            "Si el evento está cancelado, rechaza la consulta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad consultada correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento o sector inexistente"),
            @ApiResponse(responseCode = "409", description = "El evento fue cancelado"),
            @ApiResponse(responseCode = "503", description = "Servicio de Eventos y Lugares no disponible")
    })
    @GetMapping
    public ResponseEntity<DisponibilidadDTO> consultarDisponibilidad(

            @Parameter(description = "ID del evento", example = "21", required = true) @RequestParam Long eventoId,

            @Parameter(description = "ID del sector", example = "5", required = true) @RequestParam Long sectorId) {

        DisponibilidadDTO dto = disponibilidadService.consultarDisponibilidad(
                eventoId, sectorId);

        return ResponseEntity.ok(dto);
    }
}
