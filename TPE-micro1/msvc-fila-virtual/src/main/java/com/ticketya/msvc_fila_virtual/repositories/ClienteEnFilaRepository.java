
package com.ticketya.msvc_fila_virtual.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketya.msvc_fila_virtual.entities.ClienteEnFila;
import com.ticketya.msvc_fila_virtual.entities.EstadoFila;

import jakarta.persistence.LockModeType;

@Repository
public interface ClienteEnFilaRepository
        extends JpaRepository<ClienteEnFila, Long> {

    Optional<ClienteEnFila> findByEventoIdAndClienteId(
            Long eventoId,
            Long clienteId);

    long countByEventoIdAndEstado(
            Long eventoId,
            EstadoFila estado);

    long countByEventoIdAndEstadoAndIdLessThan(
            Long eventoId,
            EstadoFila estado,
            Long id);

    List<ClienteEnFila> findByEventoIdAndEstadoOrderByIdAsc(
            Long eventoId,
            EstadoFila estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ClienteEnFila> findFirstByEventoIdOrderByIdAsc(
            Long eventoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c FROM ClienteEnFila c
            WHERE c.eventoId = :eventoId
            AND c.clienteId = :clienteId
            """)
    Optional<ClienteEnFila> bloquearPorEventoYCliente(
            @Param("eventoId") Long eventoId,
            @Param("clienteId") Long clienteId);

    @Modifying
    @Query("""
            UPDATE ClienteEnFila c
            SET c.estado = :vencido
            WHERE c.estado = :habilitado
            AND c.fechaVencimiento <= :ahora
            """)
    int vencerTurnosExpirados(
            @Param("habilitado") EstadoFila habilitado,
            @Param("vencido") EstadoFila vencido,
            @Param("ahora") LocalDateTime ahora);
}
