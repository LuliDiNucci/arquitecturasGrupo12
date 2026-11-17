package com.ticketya.reservas.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketya.reservas.entities.Reserva;
import com.ticketya.reservas.enums.EstadoReserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Buscar todas las reservas de un cliente
    List<Reserva> findByClienteId(Long clienteId);

    // Buscar todas las reservas de un evento
    List<Reserva> findByEventoId(Long eventoId);

    // Buscar una reserva por su clave de idempotencia
    Optional<Reserva> findByClaveIdempotencia(String claveIdempotencia);

    @Query("""
                SELECT r.eventoId
                FROM Reserva r
                WHERE r.id = :reservaId
            """)
    Optional<Long> buscarEventoId(
            @Param("reservaId") Long reservaId);

    @Query("""
                SELECT COALESCE(SUM(d.cantidad - d.cantidadLiberada), 0)
                FROM DetalleReserva d
                JOIN d.reserva r
                WHERE r.clienteId = :clienteId
                AND r.eventoId = :eventoId
                AND (
                    r.estado = :confirmada
                    OR (
                        r.estado = :activa
                        AND r.fechaVencimiento > :ahora
                    )
                )
            """)
    Long contarEntradasDelCliente(
            @Param("clienteId") Long clienteId,
            @Param("eventoId") Long eventoId,
            @Param("confirmada") EstadoReserva confirmada,
            @Param("activa") EstadoReserva activa,
            @Param("ahora") LocalDateTime ahora);
}