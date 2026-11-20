package com.ticketya.entradas.repositories;

import com.ticketya.entradas.entities.AsignacionPersonalAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsignacionPersonalAccesoRepository extends JpaRepository<AsignacionPersonalAcceso, Long> {
    boolean existsByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);
    void deleteByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);
}
