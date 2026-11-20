package ar.edu.unicen.ticketya.eventos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unicen.ticketya.eventos.dto.ReporteEventoDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteOcupacionDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteRecaudacionDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteSectorEventoDTO;
import ar.edu.unicen.ticketya.eventos.service.ReporteEventoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/eventos/{eventoId}/reporte")
@Tag(name = "Reportes para organizadores", description = "Ventas, devoluciones, ocupacion y recaudacion por evento")
public class ReporteEventoController {

    private final ReporteEventoService reporteService;

    public ReporteEventoController(ReporteEventoService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    @Operation(summary = "Consultar el reporte general de un evento")
    @org.springframework.security.access.prepost.PreAuthorize("@eventAccess.event(#eventoId)")
    public ReporteEventoDTO obtenerReporte(@PathVariable Long eventoId) {
        return reporteService.obtenerReporte(eventoId);
    }

    @GetMapping("/sectores")
    @Operation(summary = "Consultar ventas y devoluciones por sector")
    @org.springframework.security.access.prepost.PreAuthorize("@eventAccess.event(#eventoId)")
    public List<ReporteSectorEventoDTO> obtenerReporteSectores(@PathVariable Long eventoId) {
        return reporteService.obtenerReporteSectores(eventoId);
    }

    @GetMapping("/recaudacion")
    @Operation(summary = "Consultar la recaudacion de un evento")
    @org.springframework.security.access.prepost.PreAuthorize("@eventAccess.event(#eventoId)")
    public ReporteRecaudacionDTO obtenerRecaudacion(@PathVariable Long eventoId) {
        return reporteService.obtenerRecaudacion(eventoId);
    }

    @GetMapping("/ocupacion")
    @Operation(summary = "Consultar el porcentaje de ocupacion de un evento")
    @org.springframework.security.access.prepost.PreAuthorize("@eventAccess.event(#eventoId)")
    public ReporteOcupacionDTO obtenerOcupacion(@PathVariable Long eventoId) {
        return reporteService.obtenerOcupacion(eventoId);
    }
}
