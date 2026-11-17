
package com.ticketya.compras.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketya.compras.entities.ItemCompra;

public interface ItemCompraRepository
        extends JpaRepository<ItemCompra, Long> {

    // Identificar el item asociado a una entrada emitida
    Optional<ItemCompra> findByEntradaId(Long entradaId);

    // Obtener todas las entradas individuales de una compra
    List<ItemCompra> findByDetalleCompra_Compra_Id(Long compraId);
}
