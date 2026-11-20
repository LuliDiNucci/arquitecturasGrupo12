
package com.ticketya.compras.services;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketya.compras.dtos.ReporteComprasEventoDTO;
import com.ticketya.compras.dtos.ReporteSectorDTO;
import com.ticketya.compras.repositories.DetalleCompraRepository;
import com.ticketya.compras.repositories.PagoRepository;
import com.ticketya.compras.repositories.ReintegroRepository;

@Service
public class ReporteComprasServiceImpl implements ReporteComprasService {

    private final DetalleCompraRepository detalleCompraRepository;
    private final ReintegroRepository reintegroRepository;
    private final PagoRepository pagoRepository;

    public ReporteComprasServiceImpl(
            DetalleCompraRepository detalleCompraRepository,
            ReintegroRepository reintegroRepository,
            PagoRepository pagoRepository) {

        this.detalleCompraRepository = detalleCompraRepository;
        this.reintegroRepository = reintegroRepository;
        this.pagoRepository = pagoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteComprasEventoDTO obtenerReporte(Long eventoId) {

        if (eventoId == null || eventoId <= 0) {
            throw new IllegalArgumentException("El evento debe ser válido");
        }

        Map<Long, Long> cantidadesVendidas = new LinkedHashMap<>();
        Map<Long, BigDecimal> importesBrutos = new LinkedHashMap<>();

        var ventas = detalleCompraRepository
                .obtenerVentasPorSector(eventoId);

        for (var venta : ventas) {
            cantidadesVendidas.put(
                    venta.getSectorId(),
                    venta.getCantidadVendida() == null
                            ? 0L : venta.getCantidadVendida());

            importesBrutos.put(
                    venta.getSectorId(),
                    venta.getImporteBruto() == null
                            ? BigDecimal.ZERO : venta.getImporteBruto());
        }

        Map<Long, Long> cantidadesDevueltas = new LinkedHashMap<>();
        Map<Long, BigDecimal> importesReintegrados = new LinkedHashMap<>();

        var reintegros = reintegroRepository
                .obtenerReintegrosPorSector(eventoId);

        for (var reintegro : reintegros) {
            cantidadesDevueltas.put(
                    reintegro.getSectorId(),
                    reintegro.getCantidadDevuelta() == null
                            ? 0L : reintegro.getCantidadDevuelta());

            importesReintegrados.put(
                    reintegro.getSectorId(),
                    reintegro.getImporteReintegrado() == null
                            ? BigDecimal.ZERO : reintegro.getImporteReintegrado());
        }

        Map<Long, ReporteSectorDTO> sectores = new LinkedHashMap<>();

        cantidadesVendidas.keySet().forEach(sectorId -> {
            sectores.put(
                    sectorId,
                    new ReporteSectorDTO(
                            sectorId,
                            cantidadesVendidas.get(sectorId),
                            cantidadesDevueltas.getOrDefault(sectorId, 0L),
                            importesBrutos.getOrDefault(sectorId, BigDecimal.ZERO),
                            importesReintegrados.getOrDefault(sectorId, BigDecimal.ZERO)
                    )
            );
        });

        cantidadesDevueltas.keySet().forEach(sectorId -> {
            sectores.putIfAbsent(
                    sectorId,
                    new ReporteSectorDTO(
                            sectorId,
                            0L,
                            cantidadesDevueltas.get(sectorId),
                            BigDecimal.ZERO,
                            importesReintegrados.getOrDefault(sectorId, BigDecimal.ZERO)
                    )
            );
        });

        var pagos = pagoRepository.obtenerPagosAprobadosPorEvento(eventoId);

        BigDecimal importePagado = pagos == null
                || pagos.getImportePagado() == null
                        ? BigDecimal.ZERO
                        : pagos.getImportePagado();

        BigDecimal importeReintegrado = importesReintegrados.values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal recaudacionNeta = importePagado.subtract(importeReintegrado);

        return new ReporteComprasEventoDTO(
                eventoId,
                importePagado,
                importeReintegrado,
                recaudacionNeta,
                sectores.values().stream().toList()
        );
    }
}
