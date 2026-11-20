package ar.edu.unicen.ticketya.eventos.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unicen.ticketya.eventos.dto.DisponibilidadDTO;
import ar.edu.unicen.ticketya.eventos.dto.EventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.PrecioSectorResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteComprasDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteEventoDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteOcupacionDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteRecaudacionDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteSectorComprasDTO;
import ar.edu.unicen.ticketya.eventos.dto.ReporteSectorEventoDTO;
import ar.edu.unicen.ticketya.eventos.feign.ComprasFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.ReservasFeignClient;
import ar.edu.unicen.ticketya.eventos.service.EventoService;
import ar.edu.unicen.ticketya.eventos.service.ReporteEventoService;

@Service
public class ReporteEventoServiceImpl implements ReporteEventoService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final EventoService eventoService;
    private final ComprasFeignClient comprasClient;
    private final ReservasFeignClient reservasClient;

    public ReporteEventoServiceImpl(EventoService eventoService,
            ComprasFeignClient comprasClient,
            ReservasFeignClient reservasClient) {
        this.eventoService = eventoService;
        this.comprasClient = comprasClient;
        this.reservasClient = reservasClient;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteEventoDTO obtenerReporte(Long eventoId) {
        validarEventoId(eventoId);

        EventoResponseDTO evento = eventoService.obtener(eventoId);
        ReporteComprasDTO compras = comprasClient.obtenerReporte(eventoId);
        List<ReporteSectorComprasDTO> datosCompras = compras.getSectores() == null
                ? List.of() : compras.getSectores();

        Map<Long, ReporteSectorComprasDTO> comprasPorSector = new HashMap<>();
        for (ReporteSectorComprasDTO dato : datosCompras) {
            if (dato != null && dato.getSectorId() != null) {
                comprasPorSector.put(dato.getSectorId(), dato);
            }
        }

        List<ReporteSectorEventoDTO> sectoresReporte = new ArrayList<>();
        long capacidadTotal = 0;
        long lugaresOcupadosTotal = 0;

        List<PrecioSectorResponseDTO> sectoresEvento = evento.getSectores() == null
                ? List.of() : evento.getSectores();

        for (PrecioSectorResponseDTO sector : sectoresEvento) {
            Long sectorId = sector.getSectorId();
            int capacidad = Math.max(0, sector.getCapacidad());

            ReporteSectorComprasDTO datos = comprasPorSector.get(sectorId);
            long vendidas = datos == null || datos.getCantidadVendida() == null
                    ? 0L : datos.getCantidadVendida();
            long devueltas = datos == null || datos.getCantidadDevuelta() == null
                    ? 0L : datos.getCantidadDevuelta();
            long vigentes = Math.max(0L, vendidas - devueltas);

            // Reservas considera reservas confirmadas y reservas activas no vencidas.
            // Para sectores numerados devuelve los asientos ocupados; para no numerados,
            // la cantidad disponible.
            DisponibilidadDTO disponibilidad = reservasClient.consultarDisponibilidad(eventoId, sectorId);
            long ocupados;
            if (sector.isNumerado()) {
                ocupados = disponibilidad.getAsientosOcupados() == null
                        ? 0L : disponibilidad.getAsientosOcupados().stream()
                                .filter(id -> id != null).count();
            } else {
                int disponibles = disponibilidad.getLugaresDisponibles() == null
                        ? capacidad : disponibilidad.getLugaresDisponibles();
                ocupados = Math.max(0L, (long) capacidad - disponibles);
            }
            // Las entradas devueltas ya no cuentan como lugares ocupados.
            // No restar devoluciones aquí: Reservas ya las excluye de disponibilidad.
            ocupados = Math.min(ocupados, capacidad);

            BigDecimal importeBruto = datos == null || datos.getImporteBruto() == null
                    ? BigDecimal.ZERO : datos.getImporteBruto();
            BigDecimal importeReintegrado = datos == null || datos.getImporteReintegrado() == null
                    ? BigDecimal.ZERO : datos.getImporteReintegrado();
            BigDecimal recaudacionSector = importeBruto.subtract(importeReintegrado);

            sectoresReporte.add(new ReporteSectorEventoDTO(
                    sectorId,
                    sector.getNombre(),
                    sector.isNumerado(),
                    capacidad,
                    vendidas,
                    devueltas,
                    vigentes,
                    ocupados,
                    porcentaje(ocupados, capacidad),
                    importeBruto,
                    importeReintegrado,
                    recaudacionSector));

            capacidadTotal += capacidad;
            lugaresOcupadosTotal += ocupados;
        }

        BigDecimal importePagado = compras.getImportePagado() == null
                ? BigDecimal.ZERO : compras.getImportePagado();
        BigDecimal importeReintegrado = compras.getImporteReintegrado() == null
                ? BigDecimal.ZERO : compras.getImporteReintegrado();
        BigDecimal recaudacionNeta = compras.getRecaudacionNeta() == null
                ? importePagado.subtract(importeReintegrado)
                : compras.getRecaudacionNeta();

        return new ReporteEventoDTO(
                evento.getId(),
                evento.getNombre(),
                capacidadTotal,
                lugaresOcupadosTotal,
                porcentaje(lugaresOcupadosTotal, capacidadTotal),
                importePagado,
                importeReintegrado,
                recaudacionNeta,
                sectoresReporte);
    }

    @Override
    public List<ReporteSectorEventoDTO> obtenerReporteSectores(Long eventoId) {
        return obtenerReporte(eventoId).getSectores();
    }

    @Override
    public ReporteRecaudacionDTO obtenerRecaudacion(Long eventoId) {
        ReporteEventoDTO reporte = obtenerReporte(eventoId);
        return new ReporteRecaudacionDTO(
                reporte.getEventoId(),
                reporte.getImportePagado(),
                reporte.getImporteReintegrado(),
                reporte.getRecaudacionNeta());
    }

    @Override
    public ReporteOcupacionDTO obtenerOcupacion(Long eventoId) {
        ReporteEventoDTO reporte = obtenerReporte(eventoId);
        return new ReporteOcupacionDTO(
                reporte.getEventoId(),
                reporte.getCapacidadTotal(),
                reporte.getLugaresOcupados(),
                reporte.getPorcentajeOcupacion(),
                reporte.getSectores());
    }

    private BigDecimal porcentaje(long ocupados, long capacidad) {
        if (capacidad <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(ocupados)
                .multiply(CIEN)
                .divide(BigDecimal.valueOf(capacidad), 2, RoundingMode.HALF_UP);
    }

    private void validarEventoId(Long eventoId) {
        if (eventoId == null || eventoId <= 0) {
            throw new IllegalArgumentException("El ID del evento debe ser vÃ¡lido");
        }
    }
}

