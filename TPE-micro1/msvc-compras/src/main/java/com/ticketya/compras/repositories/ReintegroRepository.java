
package com.ticketya.compras.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketya.compras.entities.Reintegro;

public interface ReintegroRepository
        extends JpaRepository<Reintegro, Long> {

    // Buscar por entrada emitida.
    Optional<Reintegro> findByEntradaId(Long entradaId);

    // Buscar por item de compra, incluso sin entrada emitida.
    Optional<Reintegro> findByItemCompra_Id(Long itemCompraId);

    // Historial de reintegros de una compra.
    List<Reintegro> findByPago_Compra_Id(Long compraId);
}
