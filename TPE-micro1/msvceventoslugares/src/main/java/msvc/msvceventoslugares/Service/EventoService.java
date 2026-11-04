package msvc.msvceventoslugares.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import msvc.msvceventoslugares.Enums.EstadoEvento;
import msvc.msvceventoslugares.Enums.TipoEvento;
import msvc.msvceventoslugares.dto.EventoDTO;
import msvc.msvceventoslugares.dto.EventoRequestDTO;
import msvc.msvceventoslugares.dto.LugarDTO;
import msvc.msvceventoslugares.dto.OrganizadorDTO;
import msvc.msvceventoslugares.dto.ParticipanteDTO;
import msvc.msvceventoslugares.dto.PrecioSectorDTO;
import msvc.msvceventoslugares.entity.Evento;
import msvc.msvceventoslugares.entity.Lugar;
import msvc.msvceventoslugares.entity.Organizador;
import msvc.msvceventoslugares.entity.Participante;
import msvc.msvceventoslugares.entity.PrecioSector;
import msvc.msvceventoslugares.repository.EventoRepository;
import msvc.msvceventoslugares.repository.EventoSectorRepository;
import msvc.msvceventoslugares.repository.LugarRepository;
import msvc.msvceventoslugares.repository.OrganizadorRepository;
import msvc.msvceventoslugares.repository.ParticipanteRepository;
import msvc.msvceventoslugares.repository.PrecioSectorRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private OrganizadorRepository organizadorRepository;

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private EventoSectorRepository eventoSectorRepository;

    @Autowired
    private PrecioSectorRepository precioSectorRepository;

    // CREAR EVENTO

    public EventoDTO crearEvento(EventoRequestDTO dto) {

        Evento evento = new Evento();

        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setTipoEvento(dto.getTipoEvento());
        evento.setFechaYHora(dto.getFechaYHora());

        Organizador organizador = organizadorRepository
                .findById(dto.getIdOrganizador())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizador inexistente"));

        Lugar lugar = lugarRepository
                .findById(dto.getIdLugar())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar inexistente"));

        evento.setOrganizador(organizador);
        evento.setLugar(lugar);

        List<Participante> participantes = participanteRepository.findAllById(dto.getIdsParticipantes());

        evento.setParticipantes(participantes);

        evento.setEstadoEvento(EstadoEvento.BORRADOR);

        eventoRepository.save(evento);

        return convertirDTO(evento);
    }

    // OBTENER TODOS

    public List<EventoDTO> obtenerEventos() {

        return eventoRepository.findAll()
                .stream()
                .map(e -> convertirDTO(e))
                .toList();
    }

    // OBTENER POR ID

    public EventoDTO obtenerEvento(Long id) {

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento inexistente"));

        return convertirDTO(evento);
    }

    // MODIFICAR EVENTO

    public EventoDTO modificarEvento(
            Long id, EventoRequestDTO dto) {

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento inexistente"));

        if (evento.getEstadoEvento() != EstadoEvento.BORRADOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden modificar eventos en borrador");
        }

        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setTipoEvento(dto.getTipoEvento());
        evento.setFechaYHora(dto.getFechaYHora());

        Organizador organizador = organizadorRepository
                .findById(dto.getIdOrganizador())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizador inexistente"));

        Lugar lugar = lugarRepository
                .findById(dto.getIdLugar())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar inexistente"));

        evento.setOrganizador(organizador);
        evento.setLugar(lugar);

        List<Participante> participantes = participanteRepository.findAllById(dto.getIdsParticipantes());

        evento.setParticipantes(participantes);

        eventoRepository.save(evento);

        return convertirDTO(evento);
    }

    // MODIFICAR PRECIOS

    @Transactional
    public EventoDTO modificarPrecios(
            Long id, List<PrecioSectorDTO> precios) {

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento inexistente"));

        for (PrecioSectorDTO precio : precios) {

            PrecioSector es = eventoSectorRepository
                    .findByEventoIdAndSectorId(id, precio.getIdSector())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector inexistente"));

            es.setPrecio(precio.getPrecio());
        }

        return convertirDTO(evento);
    }

    // PUBLICAR

    public EventoDTO publicarEvento(Long id) {

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento inexistente"));

        if (evento.getEstadoEvento() != EstadoEvento.BORRADOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El evento no está en borrador");
        }

        if (evento.getNombre() == null ||
                evento.getLugar() == null ||
                evento.getOrganizador() == null ||
                evento.getFechaYHora() == null ||
                evento.getParticipantes() == null ||
                evento.getParticipantes().isEmpty()) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La configuración del evento está incompleta");
        }

        evento.setEstadoEvento(EstadoEvento.PUBLICADO);

        eventoRepository.save(evento);

        return convertirDTO(evento);
    }

    // CANCELAR

    public EventoDTO cancelarEvento(Long id) {

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento inexistente"));

        if (evento.getEstadoEvento() != EstadoEvento.PUBLICADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden cancelar eventos publicados");
        }

        evento.setEstadoEvento(EstadoEvento.CANCELADO);

        eventoRepository.save(evento);

        return convertirDTO(evento);
    }

    // SUSPENDER

    public EventoDTO suspenderEvento(Long id) {

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento inexistente"));

        if (evento.getEstadoEvento() != EstadoEvento.PUBLICADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden suspender eventos publicados");
        }

        evento.setEstadoEvento(EstadoEvento.SUSPENDIDO);

        eventoRepository.save(evento);

        return convertirDTO(evento);
    }

    // BUSCAR EVENTOS

    public List<EventoDTO> buscarEventos(
            TipoEvento tipo,
            Long idOrganizador,
            Long idParticipante,
            String ciudad,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String ordenarPor) {

        List<Evento> eventos = eventoRepository.buscarEventos(
                tipo, idOrganizador, idParticipante,
                ciudad, fechaDesde, fechaHasta);

        if ("FECHA".equalsIgnoreCase(ordenarPor)) {
            eventos.sort(
                    Comparator.comparing(Evento::getFechaYHora));
        }

        if ("PRECIO".equalsIgnoreCase(ordenarPor)) {
            // se ordena por precio máximo de sus sectores (descendente)
            eventos.sort((e1, e2) -> {

                double precio1 = precioSectorRepository
                        .findByEventoId(e1.getId())
                        .stream()
                        .mapToDouble(PrecioSector::getPrecio)
                        .max()
                        .orElse(0);

                double precio2 = precioSectorRepository
                        .findByEventoId(e2.getId())
                        .stream()
                        .mapToDouble(PrecioSector::getPrecio)
                        .max()
                        .orElse(0);

                return Double.compare(precio2, precio1);
            });
        }

        if ("RELEVANCIA".equalsIgnoreCase(ordenarPor)) {
            // relevancia = cantidad de participantes del evento (descendente)
            eventos.sort((e1, e2) -> Integer
                    .compare(e2.getParticipantes().size(),
                             e1.getParticipantes().size()));
        }

        return eventos.stream()
                .map(e -> convertirDTO(e))
                .toList();
    }

    // CONVERTIR A DTO

    private EventoDTO convertirDTO(Evento evento) {

        EventoDTO dto = new EventoDTO();

        dto.setId(evento.getId());
        dto.setNombre(evento.getNombre());
        dto.setDescripcion(evento.getDescripcion());
        dto.setTipoEvento(evento.getTipoEvento());
        dto.setFechaYHora(evento.getFechaYHora());
        dto.setEstadoEvento(evento.getEstadoEvento());

        if (evento.getOrganizador() != null) {
            OrganizadorDTO organizadorDTO = new OrganizadorDTO();
            organizadorDTO.setId(evento.getOrganizador().getId());
            organizadorDTO.setNombre(evento.getOrganizador().getNombre());
            dto.setOrganizador(organizadorDTO);
        }

        if (evento.getLugar() != null) {
            LugarDTO lugarDTO = new LugarDTO();
            lugarDTO.setId(evento.getLugar().getId());
            lugarDTO.setNombre(evento.getLugar().getNombre());
            lugarDTO.setDireccion(evento.getLugar().getDireccion());
            lugarDTO.setCiudad(evento.getLugar().getCiudad());
            dto.setLugar(lugarDTO);
        }

        if (evento.getParticipantes() != null) {
            dto.setParticipantes(evento.getParticipantes().stream()
                    .map(p -> {
                        ParticipanteDTO participanteDTO = new ParticipanteDTO();
                        participanteDTO.setId(p.getId());
                        participanteDTO.setNombre(p.getNombre());
                        participanteDTO.setTipoParticipante(p.getTipoParticipante());
                        return participanteDTO;
                    })
                    .toList());
        }

        dto.setPrecios(precioSectorRepository.findByEventoId(evento.getId())
                .stream()
                .map(ps -> {
                    PrecioSectorDTO precioDTO = new PrecioSectorDTO();
                    precioDTO.setIdSector(ps.getSector().getId());
                    precioDTO.setNombreSector(ps.getSector().getNombre());
                    precioDTO.setPrecio(ps.getPrecio());
                    return precioDTO;
                })
                .toList());

        return dto;
    }
}