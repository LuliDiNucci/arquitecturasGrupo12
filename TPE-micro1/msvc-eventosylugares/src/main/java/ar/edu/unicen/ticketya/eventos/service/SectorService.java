package ar.edu.unicen.ticketya.eventos.service;

import java.util.List;

import ar.edu.unicen.ticketya.eventos.dto.SectorRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.SectorResponseDTO;
import ar.edu.unicen.ticketya.eventos.model.Sector;

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

    SectorResponseDTO obtenerSector(Long id);
}
