package com.ticketya.compras.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketya.compras.dtos.ReporteVentasSectorProjection;
import com.ticketya.compras.entities.DetalleCompra;

public interface DetalleCompraRepository
        extends JpaRepository<DetalleCompra, Long> {

    // Obtener todos los detalles de una compra
    List<DetalleCompra> findByCompra_Id(Long compraId);

    // Obtener las ventas originales agrupadas por sector de un evento
    @Query("""
        SELECT
            d.sectorId AS sectorId,
            SUM(d.cantidad) AS cantidadVendida,
            SUM(d.subtotal) AS importeBruto
        FROM DetalleCompra d
        JOIN d.compra c
        WHERE d.eventoId = :eventoId
          AND c.estado IN ('CONFIRMADA', 'REINTEGRO_PARCIAL', 'REINTEGRADA')
        GROUP BY d.sectorId
        """)
    List<ReporteVentasSectorProjection> obtenerVentasPorSector(
            @Param("eventoId") Long eventoId);
}
