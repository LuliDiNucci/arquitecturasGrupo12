
package com.ticketya.entradas.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketya.entradas.entities.Entrada;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    // Obtener las entradas que pertenecen a una compra.
    List<Entrada> findByCompraIdOrderByIdAsc(Long compraId);

    // Obtener las entradas de un cliente, de mas nuevas a mas viejas.
    List<Entrada> findByClienteIdOrderByFechaEmisionDescIdDesc(Long clienteId);
}
