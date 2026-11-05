package ar.edu.unicen.ticketya.eventos.service.impl;

import ar.edu.unicen.ticketya.eventos.service.*;

import ar.edu.unicen.ticketya.eventos.dto.SectorRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.SectorResponseDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.Lugar;
import ar.edu.unicen.ticketya.eventos.model.Sector;
import ar.edu.unicen.ticketya.eventos.repository.AsientoRepository;
import ar.edu.unicen.ticketya.eventos.repository.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SectorServiceImpl implements SectorService {

    private final SectorRepository sectorRepository;
    private final AsientoRepository asientoRepository;
    private final LugarService lugarService;

    public SectorServiceImpl(SectorRepository sectorRepository,
           AsientoRepository asientoRepository,
           LugarService lugarService) {
        this.sectorRepository = sectorRepository;
        this.asientoRepository = asientoRepository;
        this.lugarService = lugarService;
    }

    @Transactional
    @Override
    public SectorResponseDTO crearSector(Long lugarId, SectorRequestDTO request) {
        // Primero bloqueamos el lugar y comprobamos que se pueda editar.
        Lugar lugar = lugarService.bloquearLugar(lugarId);
        lugarService.validarLugarEditable(lugarId);

        String nombre = request.getNombre().trim();
        if (sectorRepository.existsByLugarIdAndNombreIgnoreCase(lugarId, nombre)) {
            throw ReglaNegocioException.conflicto("Ya existe un sector con ese nombre en este lugar");
        }

        Sector sector = new Sector(lugar, nombre, request.isNumerado(), request.getCapacidad());
        Sector guardado = sectorRepository.saveAndFlush(sector);
        return aSectorResponseDTO(guardado, 0);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SectorResponseDTO> listarSectores(Long lugarId) {
        lugarService.buscarLugar(lugarId);

        return sectorRepository.findByLugarIdOrderById(lugarId).stream()
                .map(sector -> aSectorResponseDTO(
                        sector,
                        asientoRepository.countBySectorId(sector.getId())
                ))
                .toList();
    }

    @Override
    public Sector buscarSector(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("Sector inexistente: " + id));
    }

    // Este bloqueo se usa cuando agregamos asientos, para evitar cambios al mismo tiempo.
    @Override
    public Sector bloquearSector(Long id) {
        return sectorRepository.bloquear(id)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("Sector inexistente: " + id));
    }

    @Override
    public SectorResponseDTO aSectorResponseDTO(Sector sector, long cantidadAsientos) {
        return new SectorResponseDTO(
                sector.getId(),
                sector.getLugar().getId(),
                sector.getNombre(),
                sector.isNumerado(),
                sector.getCapacidad(),
                cantidadAsientos
        );
    }
}
