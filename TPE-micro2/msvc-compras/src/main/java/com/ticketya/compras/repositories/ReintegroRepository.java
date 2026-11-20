

package com.ticketya.compras.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketya.compras.dtos.ReporteReintegrosSectorProjection;
import com.ticketya.compras.entities.Reintegro;

public interface ReintegroRepository
        extends JpaRepository<Reintegro, Long> {

    Optional<Reintegro> findByEntradaId(Long entradaId);

    Optional<Reintegro> findByItemCompra_Id(Long itemCompraId);

    List<Reintegro> findByPago_Compra_Id(Long compraId);

    @Query("""
        SELECT
            dc.sectorId AS sectorId,
            COUNT(r.id) AS cantidadDevuelta,
            SUM(r.importe) AS importeReintegrado
        FROM Reintegro r
        JOIN r.itemCompra ic
        JOIN ic.detalleCompra dc
        WHERE dc.eventoId = :eventoId
          AND r.estado = 'APROBADO'
        GROUP BY dc.sectorId
        """)
    List<ReporteReintegrosSectorProjection> obtenerReintegrosPorSector(
            @Param("eventoId") Long eventoId);
}
