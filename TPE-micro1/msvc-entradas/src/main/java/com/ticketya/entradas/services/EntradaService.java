package com.ticketya.entradas.services;

<<<<<<< HEAD
import java.util.List;

import com.ticketya.entradas.dtos.EntradaRequestDTO;
import com.ticketya.entradas.dtos.EntradaResponseDTO;

public interface EntradaService {

    EntradaResponseDTO emitir(EntradaRequestDTO dto);

    EntradaResponseDTO obtenerPorId(Long id);

    List<EntradaResponseDTO> obtenerPorCliente(Long clienteId);

    void invalidarPorEvento(Long eventoId);
}
=======
import com.ticketya.entradas.dtos.EntradaDTO;
import com.ticketya.entradas.dtos.ResultadoEmisionDTO;
import java.util.List;

public interface EntradaService {
    ResultadoEmisionDTO emitir(Long compraId);

    EntradaDTO obtener(Long id, Long clienteId);

    List<EntradaDTO> obtenerPorCliente(Long clienteId);
}
>>>>>>> d3b986a48b26fce11bfe7325ed2a8e4e1b3645dd
