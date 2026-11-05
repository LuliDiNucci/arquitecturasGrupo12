package com.ticketya.entradas.services;

import java.util.List;

import com.ticketya.entradas.dtos.EntradaRequestDTO;
import com.ticketya.entradas.dtos.EntradaResponseDTO;

public interface EntradaService {

    EntradaResponseDTO emitir(EntradaRequestDTO dto);

    EntradaResponseDTO obtenerPorId(Long id);

    List<EntradaResponseDTO> obtenerPorCliente(Long clienteId);

    void invalidarPorEvento(Long eventoId);
}
