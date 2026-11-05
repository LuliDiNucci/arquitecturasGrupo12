package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.dto.*;
import ar.edu.unicen.ticketya.eventos.model.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Define las operaciones de asiento.
 * La logica se encuentra en la clase de implementacion.
 */
public interface AsientoService {

    List<AsientoResponseDTO> agregarAsientos(Long sectorId, AsientosRequestDTO request);

    List<AsientoResponseDTO> listarAsientos(Long sectorId);

    AsientoResponseDTO aAsientoResponseDTO(Asiento asiento);
}
