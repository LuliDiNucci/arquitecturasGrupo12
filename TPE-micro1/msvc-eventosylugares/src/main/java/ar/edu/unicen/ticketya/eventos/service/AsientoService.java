package ar.edu.unicen.ticketya.eventos.service;

import java.util.List;

import ar.edu.unicen.ticketya.eventos.dto.AsientoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.AsientosRequestDTO;
import ar.edu.unicen.ticketya.eventos.model.Asiento;

/**
 * Define las operaciones de asiento.
 * La logica se encuentra en la clase de implementacion.
 */
public interface AsientoService {

    List<AsientoResponseDTO> agregarAsientos(Long sectorId, AsientosRequestDTO request);

    List<AsientoResponseDTO> listarAsientos(Long sectorId);

    AsientoResponseDTO aAsientoResponseDTO(Asiento asiento);

    AsientoResponseDTO obtenerAsiento(Long id);
}
