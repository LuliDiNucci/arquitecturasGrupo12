package com.ticketya.entradas.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ticketya.entradas.entities.Entrada;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    // Obtener las entradas que pertenecen a una compra.
    List<Entrada> findByCompraIdOrderByIdAsc(Long compraId);

    // Obtener las entradas de un cliente, de mas nuevas a mas viejas.
    List<Entrada> findByClienteIdOrderByFechaEmisionDescIdDesc(Long clienteId);

    List<Entrada> findByClienteId(Long clienteId);

    List<Entrada> findByEventoId(Long eventoId);

    List<Entrada> findByEventoIdAndEstado(Long eventoId, String estado);

    // Busca una entrada por su QR y bloquea el registro para evitar validaciones simultáneas.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Entrada e WHERE e.codigoQr = :codigoQr")
    Optional<Entrada> findByCodigoQrForUpdate(
        @Param("codigoQr") String codigoQr);
}
