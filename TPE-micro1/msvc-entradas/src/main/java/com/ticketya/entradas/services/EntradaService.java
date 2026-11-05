package com.ticketya.entradas.services;

import com.ticketya.entradas.dtos.EntradaDTO;
import com.ticketya.entradas.dtos.ResultadoEmisionDTO;
import java.util.List;

public interface EntradaService {
    ResultadoEmisionDTO emitir(Long compraId);

    EntradaDTO obtener(Long id, Long clienteId);

    List<EntradaDTO> obtenerPorCliente(Long clienteId);
}