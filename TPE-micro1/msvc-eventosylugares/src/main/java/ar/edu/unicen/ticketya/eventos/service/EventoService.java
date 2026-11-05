package ar.edu.unicen.ticketya.eventos.service;

import java.time.LocalDateTime;
import java.util.List;

import ar.edu.unicen.ticketya.eventos.dto.EstadoNotificacionesResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.EventoRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.EventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.EventoResumenResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.HabilitacionEventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.PrecioSectorIndividualDTO;
import ar.edu.unicen.ticketya.eventos.dto.PrecioSectorResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.PreciosEventoRequestDTO;
import ar.edu.unicen.ticketya.eventos.model.OrdenEvento;
import ar.edu.unicen.ticketya.eventos.model.TipoEvento;

/**
 * Define las operaciones de evento.
 * La logica se encuentra en la clase de implementacion.
 */
public interface EventoService {

    EventoResponseDTO crear(EventoRequestDTO request);

    EventoResponseDTO actualizar(Long id, EventoRequestDTO request);

    EventoResponseDTO configurarPrecios(Long id, PreciosEventoRequestDTO request);

    EventoResponseDTO publicar(Long id);

    EventoResponseDTO suspender(Long id);

    EventoResponseDTO cancelar(Long id);

    EventoResponseDTO marcarAgotado(Long id);

    EventoResponseDTO reabrirVentas(Long id);

    EventoResponseDTO finalizar(Long id);

    EventoResponseDTO obtener(Long id);

    List<EventoResumenResponseDTO> listar();

    List<PrecioSectorResponseDTO> ofertas(Long id);

    HabilitacionEventoResponseDTO habilitacion(Long id);

    EstadoNotificacionesResponseDTO estadoAvisos(Long id);

    List<EventoResumenResponseDTO> buscar(TipoEvento tipo,
            Long organizadorId,
            Long participanteId,
            String ciudad,
            LocalDateTime desde,
            LocalDateTime hasta,
            String texto,
            OrdenEvento ordenar,
            boolean incluirNoPublicados);

    PrecioSectorIndividualDTO obtenerPrecioSector(
            Long eventoId,
            Long sectorId);
}
