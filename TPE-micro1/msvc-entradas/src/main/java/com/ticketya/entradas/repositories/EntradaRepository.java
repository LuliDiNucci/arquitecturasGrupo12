package com.ticketya.entradas.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketya.entradas.entities.Entrada;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    List<Entrada> findByClienteId(Long clienteId);

    List<Entrada> findByEventoId(Long eventoId);

    List<Entrada> findByEventoIdAndEstado(Long eventoId, String estado);
}