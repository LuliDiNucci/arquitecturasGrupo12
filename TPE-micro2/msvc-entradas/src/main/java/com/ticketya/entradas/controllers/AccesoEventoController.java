package com.ticketya.entradas.controllers;

import com.ticketya.entradas.services.AccesoEventoService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/entradas/accesos/eventos/{eventoId}/personal/{usuarioId}")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Personal de acceso")
public class AccesoEventoController {
    private final AccesoEventoService service;
    public AccesoEventoController(AccesoEventoService service) { this.service = service; }
    @PutMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Asignar personal a un evento (administrador)")
    public ResponseEntity<Void> asignar(@PathVariable @Positive Long eventoId, @PathVariable @Positive Long usuarioId) {
        service.asignar(eventoId, usuarioId); return ResponseEntity.noContent().build();
    }
    @DeleteMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Revocar asignación de personal (administrador)")
    public ResponseEntity<Void> revocar(@PathVariable @Positive Long eventoId, @PathVariable @Positive Long usuarioId) {
        service.revocar(eventoId, usuarioId); return ResponseEntity.noContent().build();
    }
}
