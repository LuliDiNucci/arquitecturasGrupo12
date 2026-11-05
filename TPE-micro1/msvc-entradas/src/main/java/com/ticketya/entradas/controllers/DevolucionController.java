package com.ticketya.entradas.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.entradas.dtos.DevolucionRequestDTO;
import com.ticketya.entradas.dtos.DevolucionResponseDTO;
import com.ticketya.entradas.services.DevolucionService;

@RestController
@RequestMapping("/devoluciones")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(
            DevolucionService devolucionService) {

        this.devolucionService = devolucionService;
    }

    // POST /devoluciones
    @PostMapping
    public DevolucionResponseDTO solicitar(
            @RequestBody DevolucionRequestDTO dto) {

        return devolucionService.solicitar(dto);
    }

    // GET /devoluciones/pendientes
    @GetMapping("/pendientes")
    public List<DevolucionResponseDTO> obtenerPendientes() {

        return devolucionService.obtenerPendientes();
    }

    // PATCH /devoluciones/{id}/aprobar
    @PatchMapping("/{id}/aprobar")
    public DevolucionResponseDTO aprobar(
            @PathVariable Long id,
            @RequestParam Long administradorId) {

        return devolucionService.aprobar(id, administradorId);
    }

    // PATCH /devoluciones/{id}/rechazar
    @PatchMapping("/{id}/rechazar")
    public DevolucionResponseDTO rechazar(
            @PathVariable Long id,
            @RequestParam Long administradorId) {

        return devolucionService.rechazar(id, administradorId);
    }
}