package com.ticketya.reservas.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketya.reservas.entities.DetalleReserva;
import com.ticketya.reservas.enums.EstadoReserva;

public interface DetalleReservaRepository
        extends JpaRepository<DetalleReserva, Long> {

    // Buscar todos los detalles de una reserva
    List<DetalleReserva> findByReservaId(Long reservaId);

    // Buscar todos los detalles de un sector
    List<DetalleReserva> findBySectorId(Long sectorId);

    // Buscar detalles de un sector dentro de una reserva
    List<DetalleReserva> findByReservaIdAndSectorId(
            Long reservaId, Long sectorId);

    @Query("""
                SELECT COALESCE(SUM(d.cantidad - d.cantidadLiberada), 0)
                FROM DetalleReserva d
                JOIN d.reserva r
                WHERE r.eventoId = :eventoId
                AND d.sectorId = :sectorId
                AND (
                    r.estado = :confirmada
                    OR (
                        r.estado = :activa
                        AND r.fechaVencimiento > :ahora
                    )
                )
            """)
    Long contarLugaresOcupados(
            @Param("eventoId") Long eventoId,
            @Param("sectorId") Long sectorId,
            @Param("confirmada") EstadoReserva confirmada,
            @Param("activa") EstadoReserva activa,
            @Param("ahora") LocalDateTime ahora);
}