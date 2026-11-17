package com.ticketya.entradas.services;

import java.util.List;
import com.ticketya.entradas.dtos.EntradaDTO;
import com.ticketya.entradas.dtos.ResultadoEmisionDTO;

public interface EntradaService {
    ResultadoEmisionDTO emitir(Long compraId);
    EntradaDTO obtener(Long id, Long clienteId);
    List<EntradaDTO> obtenerPorCliente(Long clienteId);
    void invalidarPorEvento(Long eventoId);
}