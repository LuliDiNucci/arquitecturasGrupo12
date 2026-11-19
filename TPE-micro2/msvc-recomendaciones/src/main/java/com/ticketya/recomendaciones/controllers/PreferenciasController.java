package com.ticketya.recomendaciones.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.recomendaciones.dtos.PreferenciasEntradaDTO;
import com.ticketya.recomendaciones.dtos.PreferenciasSalidaDTO;
import com.ticketya.recomendaciones.services.PreferenciasService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/preferencias")
@Tag(name = "Preferencias", description = "Gestión de preferencias de los clientes")
public class PreferenciasController {

    private final PreferenciasService service;

    public PreferenciasController(PreferenciasService service) {
        this.service = service;
    }

    @PostMapping("/{clienteId}")
    @PreAuthorize("@clienteAccess.puedeAcceder(#clienteId)")
    @Operation(summary = "Registrar preferencias", description = "Registra las preferencias de un cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Preferencias registradas"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para registrar preferencias de otro cliente"),
            @ApiResponse(responseCode = "409", description = "El cliente ya tiene preferencias")
    })
    public ResponseEntity<PreferenciasSalidaDTO> registrar(
            @Parameter(description = "Identificador del cliente", example = "15") @PathVariable Long clienteId,
            @Valid @RequestBody PreferenciasEntradaDTO dto) {

        PreferenciasSalidaDTO respuesta = service.registrar(clienteId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @GetMapping("/{clienteId}")
    @PreAuthorize("@clienteAccess.puedeAcceder(#clienteId)")
    @Operation(summary = "Consultar preferencias", description = "Obtiene las preferencias registradas de un cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias encontradas"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para consultar preferencias de otro cliente"),
            @ApiResponse(responseCode = "404", description = "No existen preferencias")
    })
    public ResponseEntity<PreferenciasSalidaDTO> consultar(
            @Parameter(description = "Identificador del cliente", example = "15") @PathVariable Long clienteId) {

        PreferenciasSalidaDTO respuesta = service.consultar(clienteId);

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{clienteId}")
    @PreAuthorize("@clienteAccess.puedeAcceder(#clienteId)")
    @Operation(summary = "Modificar preferencias", description = "Actualiza las preferencias de un cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferencias actualizadas"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para modificar preferencias de otro cliente"),
            @ApiResponse(responseCode = "404", description = "No existen preferencias")
    })
    public ResponseEntity<PreferenciasSalidaDTO> modificar(
            @Parameter(description = "Identificador del cliente", example = "15") @PathVariable Long clienteId,
            @Valid @RequestBody PreferenciasEntradaDTO dto) {

        PreferenciasSalidaDTO respuesta = service.modificar(clienteId, dto);

        return ResponseEntity.ok(respuesta);
    }
}