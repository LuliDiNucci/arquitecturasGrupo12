package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.dto.ReporteEventoDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteOcupacionDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteRecaudacionDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteSectorEventoDTO;
import java.util.List;

public interface ReporteEventoService {
    ReporteEventoDTO obtenerReporte(Long eventoId);
    List<ReporteSectorEventoDTO> obtenerReporteSectores(Long eventoId);
    ReporteRecaudacionDTO obtenerRecaudacion(Long eventoId);
    ReporteOcupacionDTO obtenerOcupacion(Long eventoId);
}
