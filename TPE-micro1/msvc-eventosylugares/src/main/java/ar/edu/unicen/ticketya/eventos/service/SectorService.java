package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.dto.*;
import ar.edu.unicen.ticketya.eventos.model.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Define las operaciones de sector.
 * La logica se encuentra en la clase de implementacion.
 */
public interface SectorService {

    SectorResponseDTO crearSector(Long lugarId, SectorRequestDTO request);

    List<SectorResponseDTO> listarSectores(Long lugarId);

    Sector buscarSector(Long id);

    Sector bloquearSector(Long id);

    SectorResponseDTO aSectorResponseDTO(Sector sector, long cantidadAsientos);
}
