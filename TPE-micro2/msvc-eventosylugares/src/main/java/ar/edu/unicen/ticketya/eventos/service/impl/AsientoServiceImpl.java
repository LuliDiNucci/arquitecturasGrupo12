package ar.edu.unicen.ticketya.eventos.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unicen.ticketya.eventos.dto.AsientoRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.AsientoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.AsientosRequestDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.Asiento;
import ar.edu.unicen.ticketya.eventos.model.Sector;
import ar.edu.unicen.ticketya.eventos.repository.AsientoRepository;
import ar.edu.unicen.ticketya.eventos.service.AsientoService;
import ar.edu.unicen.ticketya.eventos.service.LugarService;
import ar.edu.unicen.ticketya.eventos.service.SectorService;

@Service
public class AsientoServiceImpl implements AsientoService {

    private final AsientoRepository asientoRepository;
    private final SectorService sectorService;
    private final LugarService lugarService;

    public AsientoServiceImpl(AsientoRepository asientoRepository,
            SectorService sectorService,
            LugarService lugarService) {
        this.asientoRepository = asientoRepository;
        this.sectorService = sectorService;
        this.lugarService = lugarService;
    }

    @Transactional
    @Override
    public List<AsientoResponseDTO> agregarAsientos(Long sectorId, AsientosRequestDTO request) {
        Sector sectorPrevio = sectorService.buscarSector(sectorId);

        // Mantenemos siempre el mismo orden de bloqueo: lugar y despues sector.
        lugarService.bloquearLugar(sectorPrevio.getLugar().getId());
        Sector sector = sectorService.bloquearSector(sectorId);
        lugarService.validarLugarEditable(sector.getLugar().getId());

        if (!sector.isNumerado()) {
            throw ReglaNegocioException.invalido("No se pueden cargar asientos en un sector no numerado");
        }

        long cantidadActual = asientoRepository.countBySectorId(sectorId);
        if (cantidadActual + request.getAsientos().size() > sector.getCapacidad()) {
            throw ReglaNegocioException.conflicto("Se excede la capacidad del sector: " + sector.getCapacidad());
        }

        // Revisamos los asientos que llegan para no repetir fila y numero.
        Set<String> recibidos = new HashSet<>();
        List<Asiento> nuevosAsientos = new ArrayList<>();

        for (AsientoRequestDTO dato : request.getAsientos()) {
            String fila = dato.getFila().trim().toUpperCase(Locale.ROOT);
            String clave = fila + ":" + dato.getNumero();

            boolean repetidoEnRequest = !recibidos.add(clave);
            boolean yaExiste = asientoRepository.existsBySectorIdAndFilaIgnoreCaseAndNumero(
                    sectorId,
                    fila,
                    dato.getNumero());

            if (repetidoEnRequest || yaExiste) {
                throw ReglaNegocioException.conflicto("Asiento duplicado: " + clave);
            }

            nuevosAsientos.add(new Asiento(sector, fila, dato.getNumero()));
        }

        return asientoRepository.saveAllAndFlush(nuevosAsientos).stream()
                .map(this::aAsientoResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AsientoResponseDTO> listarAsientos(Long sectorId) {
        sectorService.buscarSector(sectorId);

        return asientoRepository.findBySectorIdOrderByFilaAscNumeroAsc(sectorId).stream()
                .map(this::aAsientoResponseDTO)
                .toList();
    }

    @Override
    public AsientoResponseDTO aAsientoResponseDTO(Asiento asiento) {

        return new AsientoResponseDTO(
                asiento.getId(),
                asiento.getFila(),
                asiento.getNumero(),
                asiento.getSector().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AsientoResponseDTO obtenerAsiento(Long id) {

        Asiento asiento = asientoRepository.findById(id)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado(
                        "Asiento inexistente: " + id));

        return aAsientoResponseDTO(asiento);
    }

}
