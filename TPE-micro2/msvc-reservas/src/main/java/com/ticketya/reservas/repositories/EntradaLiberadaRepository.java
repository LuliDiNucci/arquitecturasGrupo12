
package com.ticketya.reservas.repositories;

import com.ticketya.reservas.entities.EntradaLiberada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntradaLiberadaRepository
        extends JpaRepository<EntradaLiberada, Long> {
}
