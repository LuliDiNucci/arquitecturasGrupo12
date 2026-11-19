package com.ticketya.compras.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.compras.repositories.CompraRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/compras")
@Tag(name = "Historial de compras", description = "Consulta de eventos sugeridos a partir de compras confirmadas")
public class HistorialRecomendacionesController {

    private final CompraRepository compraRepository;

    public HistorialRecomendacionesController(
            CompraRepository compraRepository) {

        this.compraRepository = compraRepository;
    }

    @GetMapping("/cliente/{clienteId}/eventos-recomendados")
    @PreAuthorize("@purchaseAccess.client(#clienteId)")
    @Operation(summary = "Obtener eventos sugeridos por historial", description = "Devuelve identificadores de eventos comprados por otros "
            + "clientes que también adquirieron entradas para eventos "
            + "comprados por el cliente consultado. "
            + "Solo considera compras confirmadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Identificadores obtenidos correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado para consultar el historial de otro cliente")
    })
    public List<Long> obtenerEventosRecomendados(
            @PathVariable Long clienteId) {

        return compraRepository
                .buscarEventosRecomendadosPorHistorial(clienteId);
    }
}