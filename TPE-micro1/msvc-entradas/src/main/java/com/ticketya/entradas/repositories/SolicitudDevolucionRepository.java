package com.ticketya.entradas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketya.entradas.entities.SolicitudDevolucion;

public interface SolicitudDevolucionRepository
        extends JpaRepository<SolicitudDevolucion, Long> {

    List<SolicitudDevolucion> findByEstado(String estado);

    Optional<SolicitudDevolucion> findByEntrada_IdAndEstado(
    Long entradaId,
    String estado
);
}