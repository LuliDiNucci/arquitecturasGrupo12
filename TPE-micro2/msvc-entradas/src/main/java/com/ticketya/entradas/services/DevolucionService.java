package com.ticketya.entradas.services;

import java.util.List;

import com.ticketya.entradas.dtos.DevolucionRequestDTO;
import com.ticketya.entradas.dtos.DevolucionResponseDTO;

public interface DevolucionService {

    DevolucionResponseDTO solicitar(DevolucionRequestDTO dto);

    List<DevolucionResponseDTO> obtenerPendientes();

    DevolucionResponseDTO aprobar(Long id, Long administradorId);

    DevolucionResponseDTO rechazar(Long id, Long administradorId);
}