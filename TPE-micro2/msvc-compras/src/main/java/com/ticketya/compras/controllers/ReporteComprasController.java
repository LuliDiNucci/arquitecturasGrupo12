package com.ticketya.compras.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.compras.dtos.ReporteComprasEventoDTO;
import com.ticketya.compras.services.ReporteComprasService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/compras")
@Tag(name = "Reportes internos", description = "Datos de ventas y reintegros para el reporte de eventos")
public class ReporteComprasController {

    private final ReporteComprasService reporteComprasService;

    public ReporteComprasController(ReporteComprasService reporteComprasService) {
        this.reporteComprasService = reporteComprasService;
    }

    @GetMapping("/evento/{eventoId}/reporte")
    @Operation(summary = "Obtener ventas, devoluciones y recaudacion de un evento (uso interno)")
    public ReporteComprasEventoDTO obtenerReporte(@PathVariable Long eventoId) {
        return reporteComprasService.obtenerReporte(eventoId);
    }
}
