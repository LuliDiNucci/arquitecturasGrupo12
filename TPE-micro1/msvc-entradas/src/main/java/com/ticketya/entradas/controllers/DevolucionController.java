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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/devoluciones")
@Tag(name = "Devoluciones", description = "Operaciones para solicitar y gestionar devoluciones de entradas")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(
            DevolucionService devolucionService) {

        this.devolucionService = devolucionService;
    }



    @Operation(summary = "Solicitar devolución",description = "Registra una solicitud de devolución para una entrada emitida. La solicitud debe realizarse con al menos 72 horas de anticipación al evento." )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Solicitud registrada",
            content = @Content(schema = @Schema(implementation = DevolucionResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "409", description = "La entrada no cumple las condiciones para solicitar una devolución")
    })
    @PostMapping
    public DevolucionResponseDTO solicitar(
            @RequestBody DevolucionRequestDTO dto) {

        return devolucionService.solicitar(dto);
    }




    @Operation(summary = "Obtener devoluciones pendientes",description = "Devuelve todas las solicitudes de devolución que todavía no fueron resueltas.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado de solicitudes pendientes",
            content = @Content(array = @ArraySchema(
                schema = @Schema(implementation = DevolucionResponseDTO.class)
            ))
        )
    })
    @GetMapping("/pendientes")
    public List<DevolucionResponseDTO> obtenerPendientes() {

        return devolucionService.obtenerPendientes();
    }



    @Operation(summary = "Aprobar devolución",description = "Aprueba una solicitud pendiente, invalida la entrada y solicita la liberación del lugar y el reintegro correspondiente.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Devolución aprobada",
            content = @Content(schema = @Schema(implementation = DevolucionResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Solicitud inexistente")
    })
    @PatchMapping("/{id}/aprobar")
    public DevolucionResponseDTO aprobar(
            @PathVariable Long id,
            @RequestParam Long administradorId) {

        return devolucionService.aprobar(id, administradorId);
    }




    @Operation(summary = "Rechazar devolución",description = "Rechaza una solicitud pendiente. La entrada permanece emitida.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Devolución rechazada",
            content = @Content(schema = @Schema(implementation = DevolucionResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Solicitud inexistente")
    })
    @PatchMapping("/{id}/rechazar")
    public DevolucionResponseDTO rechazar(
            @PathVariable Long id,
            @RequestParam Long administradorId) {

        return devolucionService.rechazar(id, administradorId);
    }
}