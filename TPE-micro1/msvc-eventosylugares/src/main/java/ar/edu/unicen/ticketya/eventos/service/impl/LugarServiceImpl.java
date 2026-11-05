package ar.edu.unicen.ticketya.eventos.service.impl;

import ar.edu.unicen.ticketya.eventos.service.*;

import ar.edu.unicen.ticketya.eventos.dto.LugarRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.LugarResponseDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.EstadoEvento;
import ar.edu.unicen.ticketya.eventos.model.Lugar;
import ar.edu.unicen.ticketya.eventos.repository.EventoRepository;
import ar.edu.unicen.ticketya.eventos.repository.LugarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LugarServiceImpl implements LugarService {

    private final LugarRepository lugarRepository;
    private final EventoRepository eventoRepository;

    public LugarServiceImpl(LugarRepository lugarRepository, EventoRepository eventoRepository) {
        this.lugarRepository = lugarRepository;
        this.eventoRepository = eventoRepository;
    }

    @Transactional
    @Override
    public LugarResponseDTO crearLugar(LugarRequestDTO request) {
        Lugar lugar = new Lugar(
                request.getNombre().trim(),
                request.getDireccion().trim(),
                request.getCiudad().trim(),
                request.getLatitud(),
                request.getLongitud()
        );

        Lugar guardado = lugarRepository.save(lugar);
        return aLugarResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LugarResponseDTO> listarLugares() {
        return lugarRepository.findAll().stream()
                .map(this::aLugarResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public LugarResponseDTO obtenerLugar(Long id) {
        return aLugarResponseDTO(buscarLugar(id));
    }

    @Override
    public Lugar buscarLugar(Long id) {
        return lugarRepository.findById(id)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("Lugar inexistente: " + id));
    }

    // Bloqueamos el lugar cuando cambiamos su estructura o publicamos un evento.
    // Asi evitamos que se agreguen sectores justo durante la publicacion.
    @Override
    public Lugar bloquearLugar(Long id) {
        return lugarRepository.bloquear(id)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("Lugar inexistente: " + id));
    }

    @Override
    public void validarLugarEditable(Long lugarId) {
        boolean tieneEventosActivos = eventoRepository.existsByLugarIdAndEstadoIn(
                lugarId,
                List.of(EstadoEvento.PUBLICADO, EstadoEvento.AGOTADO, EstadoEvento.SUSPENDIDO)
        );

        if (tieneEventosActivos) {
            throw ReglaNegocioException.conflicto(
                    "No se puede alterar un lugar con eventos activos o suspendidos"
            );
        }
    }

    @Override
    public LugarResponseDTO aLugarResponseDTO(Lugar lugar) {
        return new LugarResponseDTO(
                lugar.getId(),
                lugar.getNombre(),
                lugar.getDireccion(),
                lugar.getCiudad(),
                lugar.getLatitud(),
                lugar.getLongitud()
        );
    }
}
