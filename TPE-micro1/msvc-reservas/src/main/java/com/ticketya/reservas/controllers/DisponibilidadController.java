
package com.ticketya.reservas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.reservas.dtos.DisponibilidadDTO;
import com.ticketya.reservas.services.DisponibilidadService;

@RestController
@RequestMapping("/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(
            DisponibilidadService disponibilidadService) {

        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping
    public ResponseEntity<DisponibilidadDTO> consultarDisponibilidad(
            @RequestParam Long eventoId,
            @RequestParam Long sectorId) {

        DisponibilidadDTO dto = disponibilidadService.consultarDisponibilidad(
                eventoId, sectorId);

        return ResponseEntity.ok(dto);
    }
}
