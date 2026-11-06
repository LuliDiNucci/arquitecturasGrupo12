
package com.ticketya.compras.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketya.compras.entities.DetalleCompra;

public interface DetalleCompraRepository
        extends JpaRepository<DetalleCompra, Long> {

    // Obtener todos los detalles de una compra
    List<DetalleCompra> findByCompra_Id(Long compraId);
}
