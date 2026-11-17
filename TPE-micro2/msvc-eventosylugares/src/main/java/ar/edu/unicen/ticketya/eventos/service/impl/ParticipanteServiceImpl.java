package ar.edu.unicen.ticketya.eventos.service.impl;

import ar.edu.unicen.ticketya.eventos.service.*;

import ar.edu.unicen.ticketya.eventos.dto.ParticipanteRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.ParticipanteResponseDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.Participante;
import ar.edu.unicen.ticketya.eventos.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class ParticipanteServiceImpl implements ParticipanteService {

    private final ParticipanteRepository participanteRepository;

    public ParticipanteServiceImpl(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }

    @Transactional
    @Override
    public ParticipanteResponseDTO crearParticipante(ParticipanteRequestDTO request) {
        Participante participante = new Participante(request.getNombre().trim(), request.getTipo());
        return aParticipanteResponseDTO(participanteRepository.save(participante));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipanteResponseDTO> listarParticipantes() {
        return participanteRepository.findAll().stream()
                .map(this::aParticipanteResponseDTO)
                .toList();
    }

    @Override
    public List<Participante> buscarParticipantes(List<Long> ids) {
        // Verificamos que no haya participantes repetidos en el mismo evento.
        if (new HashSet<>(ids).size() != ids.size()) {
            throw ReglaNegocioException.invalido("No se pueden repetir participantes en un evento");
        }

        List<Participante> encontrados = participanteRepository.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw ReglaNegocioException.noEncontrado("Uno o mas participantes no existen");
        }

        return encontrados;
    }

    @Override
    public ParticipanteResponseDTO aParticipanteResponseDTO(Participante participante) {
        return new ParticipanteResponseDTO(
                participante.getId(),
                participante.getNombre(),
                participante.getTipo()
        );
    }
}
