package ar.edu.unicen.ticketya.eventos.service;

import ar.edu.unicen.ticketya.eventos.dto.*;
import ar.edu.unicen.ticketya.eventos.model.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Define las operaciones de participante.
 * La logica se encuentra en la clase de implementacion.
 */
public interface ParticipanteService {

    ParticipanteResponseDTO crearParticipante(ParticipanteRequestDTO request);

    List<ParticipanteResponseDTO> listarParticipantes();

    List<Participante> buscarParticipantes(List<Long> ids);

    ParticipanteResponseDTO aParticipanteResponseDTO(Participante participante);
}
