package com.ticketya.reservas.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketya.reservas.entities.AsignacionAsiento;
import com.ticketya.reservas.enums.EstadoReserva;

public interface AsignacionAsientoRepository
        extends JpaRepository<AsignacionAsiento, Long> {

    // Buscar las asignaciones de un detalle de reserva
    List<AsignacionAsiento> findByDetalleReservaId(Long detalleReservaId);

    // Buscar todas las asignaciones de un asiento
    List<AsignacionAsiento> findByAsientoId(Long asientoId);

    // Buscar un asiento dentro de un detalle de reserva
    List<AsignacionAsiento> findByDetalleReservaIdAndAsientoId(
            Long detalleReservaId, Long asientoId);

    @Query("""
                SELECT a.asientoId
                FROM AsignacionAsiento a
                JOIN a.detalleReserva d
                JOIN d.reserva r
                WHERE r.eventoId = :eventoId
                AND d.sectorId = :sectorId
                AND a.liberado = false
                AND (
                    r.estado = :confirmada
                    OR (
                        r.estado = :activa
                        AND r.fechaVencimiento > :ahora
                    )
                )
            """)
    List<Long> buscarAsientosOcupados(
            @Param("eventoId") Long eventoId,
            @Param("sectorId") Long sectorId,
            @Param("confirmada") EstadoReserva confirmada,
            @Param("activa") EstadoReserva activa,
            @Param("ahora") LocalDateTime ahora);

}