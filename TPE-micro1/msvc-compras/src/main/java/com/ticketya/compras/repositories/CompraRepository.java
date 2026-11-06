
package com.ticketya.compras.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticketya.compras.entities.Compra;

import jakarta.persistence.LockModeType;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    // Buscar una compra por la reserva asociada
    Optional<Compra> findByReservaId(Long reservaId);

    // Buscar compras de un evento
    List<Compra> findByEventoId(Long eventoId);

    // Bloquear una compra para evitar operaciones simultáneas
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compra c WHERE c.id = :compraId")
    Optional<Compra> bloquearCompra(
            @Param("compraId") Long compraId);
}
