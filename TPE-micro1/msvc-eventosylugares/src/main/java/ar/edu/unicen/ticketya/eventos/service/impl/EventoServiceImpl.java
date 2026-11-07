package ar.edu.unicen.ticketya.eventos.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unicen.ticketya.eventos.dto.AsientoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.EstadoNotificacionesResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.EventoRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.EventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.EventoResumenResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.HabilitacionEventoResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.ParticipanteResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.PrecioSectorIndividualDTO;
import ar.edu.unicen.ticketya.eventos.dto.PrecioSectorRequestDTO;
import ar.edu.unicen.ticketya.eventos.dto.PrecioSectorResponseDTO;
import ar.edu.unicen.ticketya.eventos.dto.PreciosEventoRequestDTO;
import ar.edu.unicen.ticketya.eventos.exception.ReglaNegocioException;
import ar.edu.unicen.ticketya.eventos.model.Asiento;
import ar.edu.unicen.ticketya.eventos.model.EstadoEvento;
import ar.edu.unicen.ticketya.eventos.model.Evento;
import ar.edu.unicen.ticketya.eventos.model.EventoParticipante;
import ar.edu.unicen.ticketya.eventos.model.NotificacionCancelacion;
import ar.edu.unicen.ticketya.eventos.model.OrdenEvento;
import ar.edu.unicen.ticketya.eventos.model.Participante;
import ar.edu.unicen.ticketya.eventos.model.PrecioSector;
import ar.edu.unicen.ticketya.eventos.model.Sector;
import ar.edu.unicen.ticketya.eventos.model.TipoEvento;
import ar.edu.unicen.ticketya.eventos.repository.AsientoRepository;
import ar.edu.unicen.ticketya.eventos.repository.EventoParticipanteRepository;
import ar.edu.unicen.ticketya.eventos.repository.EventoRepository;
import ar.edu.unicen.ticketya.eventos.repository.NotificacionCancelacionRepository;
import ar.edu.unicen.ticketya.eventos.repository.PrecioSectorRepository;
import ar.edu.unicen.ticketya.eventos.repository.SectorRepository;
import ar.edu.unicen.ticketya.eventos.service.AsientoService;
import ar.edu.unicen.ticketya.eventos.service.EventoService;
import ar.edu.unicen.ticketya.eventos.service.LugarService;
import ar.edu.unicen.ticketya.eventos.service.OrganizadorService;
import ar.edu.unicen.ticketya.eventos.service.ParticipanteService;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Service
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final SectorRepository sectorRepository;
    private final AsientoRepository asientoRepository;
    private final PrecioSectorRepository precioSectorRepository;
    private final EventoParticipanteRepository eventoParticipanteRepository;
    private final NotificacionCancelacionRepository notificacionCancelacionRepository;

    private final LugarService lugarService;
    private final OrganizadorService organizadorService;
    private final ParticipanteService participanteService;
    private final AsientoService asientoService;
    private final Clock clock;

    public EventoServiceImpl(EventoRepository eventoRepository,
            SectorRepository sectorRepository,
            AsientoRepository asientoRepository,
            PrecioSectorRepository precioSectorRepository,
            EventoParticipanteRepository eventoParticipanteRepository,
            NotificacionCancelacionRepository notificacionCancelacionRepository,
            LugarService lugarService,
            OrganizadorService organizadorService,
            ParticipanteService participanteService,
            AsientoService asientoService,
            Clock clock) {

        this.eventoRepository = eventoRepository;
        this.sectorRepository = sectorRepository;
        this.asientoRepository = asientoRepository;
        this.precioSectorRepository = precioSectorRepository;
        this.eventoParticipanteRepository = eventoParticipanteRepository;
        this.notificacionCancelacionRepository = notificacionCancelacionRepository;
        this.lugarService = lugarService;
        this.organizadorService = organizadorService;
        this.participanteService = participanteService;
        this.asientoService = asientoService;
        this.clock = clock;
    }

    // Cuando se crea un evento queda en borrador hasta que se carguen los precios.
    @Transactional
    @Override
    public EventoResponseDTO crear(EventoRequestDTO request) {
        Evento evento = new Evento(
                request.getNombre().trim(),
                request.getDescripcion().trim(),
                request.getTipo(),
                organizadorService.buscarOrganizador(request.getOrganizadorId()),
                lugarService.buscarLugar(request.getLugarId()),
                request.getFechaHora(),
                EstadoEvento.BORRADOR,
                request.isAltaDemanda());

        eventoRepository.save(evento);

        // Relacionamos los participantes con el evento que acabamos de crear.
        List<Participante> participantes = participanteService.buscarParticipantes(request.getParticipantesIds());
        for (Participante participante : participantes) {
            eventoParticipanteRepository.save(new EventoParticipante(evento, participante));
        }

        return detalle(evento);
    }

    // Un evento publicado no puede cambiar de lugar, fecha ni organizador.
    @Transactional
    @Override
    public EventoResponseDTO actualizar(Long id, EventoRequestDTO request) {
        Evento evento = buscarEvento(id);
        boolean esBorrador = evento.getEstado() == EstadoEvento.BORRADOR;
        boolean estaPublicado = evento.getEstado() == EstadoEvento.PUBLICADO
                || evento.getEstado() == EstadoEvento.AGOTADO
                || evento.getEstado() == EstadoEvento.SUSPENDIDO;

        if (!esBorrador && !estaPublicado) {
            throw ReglaNegocioException.conflicto("No se puede editar un evento cancelado o finalizado");
        }

        boolean cambiaLugar = !evento.getLugar().getId().equals(request.getLugarId());
        boolean cambiaDatosDeReserva = cambiaLugar
                || !evento.getFechaHora().equals(request.getFechaHora())
                || !evento.getOrganizador().getId().equals(request.getOrganizadorId())
                || evento.isAltaDemanda() != request.isAltaDemanda();

        if (estaPublicado && cambiaDatosDeReserva) {
            throw ReglaNegocioException.conflicto(
                    "En eventos publicados solo pueden cambiar nombre, descripcion, tipo y participantes");
        }

        evento.setNombre(request.getNombre().trim());
        evento.setDescripcion(request.getDescripcion().trim());
        evento.setTipo(request.getTipo());
        evento.setFechaHora(request.getFechaHora());
        evento.setOrganizador(organizadorService.buscarOrganizador(request.getOrganizadorId()));
        evento.setLugar(lugarService.buscarLugar(request.getLugarId()));
        evento.setAltaDemanda(request.isAltaDemanda());

        // Actualizamos las relaciones con participantes, agregando y quitando los
        // necesarios.
        List<Participante> nuevosParticipantes = participanteService.buscarParticipantes(
                request.getParticipantesIds());
        List<EventoParticipante> actuales = eventoParticipanteRepository.findByEventoId(id);

        Set<Long> nuevosIds = nuevosParticipantes.stream()
                .map(Participante::getId)
                .collect(Collectors.toSet());

        Set<Long> anterioresIds = actuales.stream()
                .map(vinculo -> vinculo.getParticipante().getId())
                .collect(Collectors.toSet());

        List<EventoParticipante> paraEliminar = actuales.stream()
                .filter(vinculo -> !nuevosIds.contains(vinculo.getParticipante().getId()))
                .toList();
        eventoParticipanteRepository.deleteAll(paraEliminar);

        for (Participante participante : nuevosParticipantes) {
            if (!anterioresIds.contains(participante.getId())) {
                eventoParticipanteRepository.save(new EventoParticipante(evento, participante));
            }
        }

        if (cambiaLugar) {
            // Los precios del lugar anterior ya no sirven para el nuevo lugar.
            precioSectorRepository.deleteAll(precioSectorRepository.findByEventoId(id));
        }

        eventoRepository.saveAndFlush(evento);
        return detalle(evento);
    }

    // Cada sector del lugar tiene un precio distinto para cada evento.
    @Transactional
    @Override
    public EventoResponseDTO configurarPrecios(Long id, PreciosEventoRequestDTO request) {
        Evento evento = buscarEvento(id);
        exigirEstado(evento, EstadoEvento.BORRADOR, "Los precios solo se pueden modificar en BORRADOR");

        List<Sector> sectoresDelLugar = sectorRepository.findByLugarIdOrderById(evento.getLugar().getId());
        Set<Long> idsNecesarios = sectoresDelLugar.stream()
                .map(Sector::getId)
                .collect(Collectors.toSet());

        Set<Long> idsRecibidos = new HashSet<>();
        for (PrecioSectorRequestDTO precioRequest : request.getPrecios()) {
            if (!idsRecibidos.add(precioRequest.getSectorId())) {
                throw ReglaNegocioException.invalido(
                        "Sector duplicado en la lista de precios: " + precioRequest.getSectorId());
            }
        }

        if (!idsNecesarios.equals(idsRecibidos)) {
            throw ReglaNegocioException.invalido(
                    "Debe enviarse exactamente un precio por cada sector del lugar");
        }

        Map<Long, Sector> sectoresPorId = sectoresDelLugar.stream()
                .collect(Collectors.toMap(Sector::getId, Function.identity()));

        Map<Long, PrecioSector> preciosExistentes = precioSectorRepository.findByEventoId(id).stream()
                .collect(Collectors.toMap(precio -> precio.getSector().getId(), Function.identity()));

        for (PrecioSectorRequestDTO precioRequest : request.getPrecios()) {
            PrecioSector precioSector = preciosExistentes.get(precioRequest.getSectorId());

            if (precioSector == null) {
                precioSector = new PrecioSector(
                        evento,
                        sectoresPorId.get(precioRequest.getSectorId()),
                        precioRequest.getPrecio());
            } else {
                precioSector.setPrecio(precioRequest.getPrecio());
            }

            precioSectorRepository.save(precioSector);
        }

        return detalle(evento);
    }

    // Antes de publicar revisamos que los datos del evento esten completos.
    @Transactional
    @Override
    public EventoResponseDTO publicar(Long id) {
        Evento evento = buscarEvento(id);
        
        if (evento.getEstado() == EstadoEvento.PUBLICADO) {
            return detalle(evento);
        }

        if (evento.getEstado() != EstadoEvento.BORRADOR
                && evento.getEstado() != EstadoEvento.SUSPENDIDO) {
            throw ReglaNegocioException.conflicto(
                    "Solo un evento BORRADOR o SUSPENDIDO puede publicarse");
        }

        // Bloqueamos el lugar para que no agreguen sectores mientras validamos.
        lugarService.bloquearLugar(evento.getLugar().getId());

        if (!evento.getFechaHora().isAfter(LocalDateTime.now(clock))) {
            throw ReglaNegocioException.conflicto("La fecha del evento debe ser futura para publicar");
        }

        if (eventoParticipanteRepository.findByEventoId(id).isEmpty()) {
            throw ReglaNegocioException.conflicto("El evento necesita al menos un participante");
        }

        List<Sector> sectoresDelLugar = sectorRepository.findByLugarIdOrderById(evento.getLugar().getId());
        if (sectoresDelLugar.isEmpty()) {
            throw ReglaNegocioException.conflicto("El lugar no tiene sectores");
        }

        Set<Long> idsSectores = sectoresDelLugar.stream()
                .map(Sector::getId)
                .collect(Collectors.toSet());

        Set<Long> sectoresConPrecio = precioSectorRepository.findByEventoId(id).stream()
                .map(precio -> precio.getSector().getId())
                .collect(Collectors.toSet());

        if (!idsSectores.equals(sectoresConPrecio)) {
            throw ReglaNegocioException.conflicto("Faltan precios de uno o mas sectores del evento");
        }

        // En un sector numerado cargamos exactamente tantos asientos como capacidad
        // tiene.
        for (Sector sector : sectoresDelLugar) {
            long cantidadAsientos = asientoRepository.countBySectorId(sector.getId());

            if (sector.isNumerado() && cantidadAsientos != sector.getCapacidad()) {
                throw ReglaNegocioException.conflicto(
                        "El sector numerado " + sector.getNombre()
                                + " requiere " + sector.getCapacidad()
                                + " asientos y tiene " + cantidadAsientos);
            }

            if (!sector.isNumerado() && cantidadAsientos != 0) {
                throw ReglaNegocioException.conflicto("Un sector no numerado no debe tener asientos");
            }
        }

        // Si estaba suspendido y antes estaba agotado, conservamos el estado AGOTADO.
        EstadoEvento nuevoEstado = evento.getEstado() == EstadoEvento.SUSPENDIDO
                && evento.getEstadoPrevioSuspension() == EstadoEvento.AGOTADO
                        ? EstadoEvento.AGOTADO
                        : EstadoEvento.PUBLICADO;

        evento.setEstado(nuevoEstado);
        evento.setEstadoPrevioSuspension(null);
        eventoRepository.saveAndFlush(evento);
        return detalle(evento);
    }

    @Transactional
    @Override
    public EventoResponseDTO suspender(Long id) {
        Evento evento = buscarEvento(id);

        if (evento.getEstado() != EstadoEvento.PUBLICADO
                && evento.getEstado() != EstadoEvento.AGOTADO) {
            throw ReglaNegocioException.conflicto(
                    "Solo se pueden suspender eventos PUBLICADOS o AGOTADOS");
        }

        evento.setEstadoPrevioSuspension(evento.getEstado());
        evento.setEstado(EstadoEvento.SUSPENDIDO);
        eventoRepository.saveAndFlush(evento);
        return detalle(evento);
    }

    // La cancelacion genera un aviso que despues pueden recibir Reservas, Entradas
    // y Compras.
    @Transactional
    @Override
    public EventoResponseDTO cancelar(Long id) {
        Evento evento = buscarEvento(id);

        if (evento.getEstado() == EstadoEvento.FINALIZADO) {
            throw ReglaNegocioException.conflicto("No se puede cancelar un evento FINALIZADO");
        }

        if (evento.getEstado() != EstadoEvento.CANCELADO) {
            evento.setEstado(EstadoEvento.CANCELADO);
            eventoRepository.saveAndFlush(evento);

            // Se guarda en la misma transaccion para no perder la cancelacion.
            NotificacionCancelacion aviso = new NotificacionCancelacion(
                    id, false, false, 0, LocalDateTime.now(clock), null, null);
            notificacionCancelacionRepository.save(aviso);
        }

        return detalle(evento);
    }

    @Transactional
    @Override
    public EventoResponseDTO marcarAgotado(Long id) {
        Evento evento = buscarEvento(id);
        exigirEstado(evento, EstadoEvento.PUBLICADO, "Solo se puede agotar un evento PUBLICADO");

        evento.setEstado(EstadoEvento.AGOTADO);
        eventoRepository.saveAndFlush(evento);
        return detalle(evento);
    }

    @Transactional
    @Override
    public EventoResponseDTO reabrirVentas(Long id) {
        Evento evento = buscarEvento(id);
        exigirEstado(evento, EstadoEvento.AGOTADO, "Solo se pueden reabrir ventas de un evento AGOTADO");

        if (!evento.getFechaHora().isAfter(LocalDateTime.now(clock))) {
            throw ReglaNegocioException.conflicto("No se pueden abrir ventas de un evento pasado");
        }

        evento.setEstado(EstadoEvento.PUBLICADO);
        eventoRepository.saveAndFlush(evento);
        return detalle(evento);
    }

    @Transactional
    @Override
    public EventoResponseDTO finalizar(Long id) {
        Evento evento = buscarEvento(id);

        if (evento.getEstado() != EstadoEvento.PUBLICADO
                && evento.getEstado() != EstadoEvento.AGOTADO
                && evento.getEstado() != EstadoEvento.SUSPENDIDO) {
            throw ReglaNegocioException.conflicto(
                    "El evento no se puede finalizar desde su estado actual");
        }

        if (evento.getFechaHora().isAfter(LocalDateTime.now(clock))) {
            throw ReglaNegocioException.conflicto("No se puede finalizar un evento futuro");
        }

        evento.setEstado(EstadoEvento.FINALIZADO);
        eventoRepository.saveAndFlush(evento);
        return detalle(evento);
    }

    @Transactional(readOnly = true)
    @Override
    public EventoResponseDTO obtener(Long id) {
        return detalle(buscarEvento(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventoResumenResponseDTO> listar() {
        return resumir(eventoRepository.findAllByOrderByFechaHoraAsc(), OrdenEvento.FECHA_ASC, null);
    }

    // Reservas usa esta consulta para conocer la distribucion de sectores y
    // precios.
    @Transactional(readOnly = true)
    @Override
    public List<PrecioSectorResponseDTO> ofertas(Long id) {
        return construirOfertas(buscarEvento(id));
    }

    @Transactional(readOnly = true)
    @Override
    public HabilitacionEventoResponseDTO habilitacion(Long id) {
        Evento evento = buscarEvento(id);
        boolean habilitado = evento.getEstado() == EstadoEvento.PUBLICADO
                && evento.getFechaHora().isAfter(LocalDateTime.now(clock));

        return new HabilitacionEventoResponseDTO(
                id, evento.getEstado(), habilitado, evento.getFechaHora());
    }

    @Transactional(readOnly = true)
    @Override
    public EstadoNotificacionesResponseDTO estadoAvisos(Long id) {
        buscarEvento(id);

        NotificacionCancelacion aviso = notificacionCancelacionRepository.findByEventoId(id)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado(
                        "El evento no tiene notificaciones de cancelacion"));

        return new EstadoNotificacionesResponseDTO(
                id,
                aviso.isReservasNotificadas(),
                aviso.isEntradasNotificadas(),
                aviso.isComprasNotificadas(),
                aviso.getIntentos(),
                aviso.getUltimoError());
    }

    // Cada parametro se aplica solamente si el usuario lo mando.
    @Transactional(readOnly = true)
    @Override
    public List<EventoResumenResponseDTO> buscar(TipoEvento tipo,
            Long organizadorId,
            Long participanteId,
            String ciudad,
            LocalDateTime desde,
            LocalDateTime hasta,
            String texto,
            OrdenEvento ordenar,
            boolean incluirNoPublicados) {

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw ReglaNegocioException.invalido("El parametro desde no puede ser posterior a hasta");
        }

        String textoNormalizado = normalizar(texto);
        String ciudadNormalizada = normalizar(ciudad);

        Specification<Evento> filtro = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();

            if (!incluirNoPublicados) {
                condiciones.add(root.get("estado").in(EstadoEvento.PUBLICADO, EstadoEvento.AGOTADO));
            }

            if (tipo != null) {
                condiciones.add(cb.equal(root.get("tipo"), tipo));
            }

            if (organizadorId != null) {
                condiciones.add(cb.equal(root.get("organizador").get("id"), organizadorId));
            }

            if (desde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), desde));
            }

            if (hasta != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("fechaHora"), hasta));
            }

            if (ciudadNormalizada != null) {
                condiciones.add(cb.like(
                        cb.lower(root.get("lugar").get("ciudad")),
                        "%" + ciudadNormalizada + "%"));
            }

            // Para filtrar por participante buscamos sus relaciones con eventos.
            if (participanteId != null) {
                Subquery<Long> subconsulta = query.subquery(Long.class);
                Root<EventoParticipante> vinculo = subconsulta.from(EventoParticipante.class);

                subconsulta.select(vinculo.get("id")).where(
                        cb.equal(vinculo.get("evento").get("id"), root.get("id")),
                        cb.equal(vinculo.get("participante").get("id"), participanteId));
                condiciones.add(cb.exists(subconsulta));
            }

            // El texto puede coincidir con el nombre, descripcion, organizador o
            // participante.
            if (textoNormalizado != null) {
                Subquery<Long> subconsultaTexto = query.subquery(Long.class);
                Root<EventoParticipante> vinculo = subconsultaTexto.from(EventoParticipante.class);

                subconsultaTexto.select(vinculo.get("id")).where(
                        cb.equal(vinculo.get("evento").get("id"), root.get("id")),
                        cb.like(
                                cb.lower(vinculo.get("participante").get("nombre")),
                                "%" + textoNormalizado + "%"));

                condiciones.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), "%" + textoNormalizado + "%"),
                        cb.like(cb.lower(root.get("descripcion")), "%" + textoNormalizado + "%"),
                        cb.like(cb.lower(root.get("organizador").get("nombre")), "%" + textoNormalizado + "%"),
                        cb.exists(subconsultaTexto)));
            }

            return cb.and(condiciones.toArray(new Predicate[0]));
        };

        return resumir(eventoRepository.findAll(filtro), ordenar, textoNormalizado);
    }

    private String normalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.toLowerCase(Locale.ROOT).trim();
    }

    // Buscamos el precio mas bajo de cada evento para mostrarlo en el listado.
    private List<EventoResumenResponseDTO> resumir(List<Evento> lista,
            OrdenEvento orden,
            String texto) {
        if (lista.isEmpty()) {
            return List.of();
        }

        List<Long> ids = lista.stream().map(Evento::getId).toList();
        Map<Long, BigDecimal> preciosMinimos = new HashMap<>();

        for (PrecioSector precio : precioSectorRepository.findByEventoIdIn(ids)) {
            preciosMinimos.merge(
                    precio.getEvento().getId(),
                    precio.getPrecio(),
                    BigDecimal::min);
        }

        Map<Long, List<String>> nombresParticipantes = new HashMap<>();
        for (EventoParticipante vinculo : eventoParticipanteRepository.findByEventoIdIn(ids)) {
            nombresParticipantes.computeIfAbsent(
                    vinculo.getEvento().getId(),
                    clave -> new ArrayList<>()).add(vinculo.getParticipante().getNombre());
        }

        OrdenEvento ordenElegido = orden == null ? OrdenEvento.FECHA_ASC : orden;
        Comparator<Evento> comparador = switch (ordenElegido) {
            case FECHA_ASC -> Comparator.comparing(Evento::getFechaHora);
            case FECHA_DESC -> Comparator.comparing(Evento::getFechaHora).reversed();
            case PRECIO_ASC -> Comparator.comparing(
                    (Evento evento) -> preciosMinimos.get(evento.getId()),
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case PRECIO_DESC -> Comparator.comparing(
                    (Evento evento) -> preciosMinimos.get(evento.getId()),
                    Comparator.nullsLast(Comparator.reverseOrder()));
            case RELEVANCIA -> Comparator.comparingInt(
                    (Evento evento) -> puntaje(
                            evento,
                            texto,
                            nombresParticipantes.getOrDefault(evento.getId(), List.of())))
                    .reversed();
        };

        List<Evento> ordenados = new ArrayList<>(lista);
        ordenados.sort(
                comparador.thenComparing(Evento::getFechaHora).thenComparing(Evento::getId));

        return ordenados.stream()
                .map(evento -> new EventoResumenResponseDTO(
                        evento.getId(),
                        evento.getNombre(),
                        evento.getTipo(),
                        evento.getEstado(),
                        evento.getFechaHora(),
                        evento.getLugar().getCiudad(),
                        evento.getLugar().getNombre(),
                        evento.getOrganizador().getNombre(),
                        preciosMinimos.get(evento.getId()),
                        evento.isAltaDemanda()))
                .toList();
    }

    private int puntaje(Evento evento, String texto, List<String> nombres) {
        if (texto == null) {
            return 0;
        }

        int puntos = 0;
        if (evento.getNombre().toLowerCase(Locale.ROOT).contains(texto)) {
            puntos += 5;
        }
        if (evento.getDescripcion().toLowerCase(Locale.ROOT).contains(texto)) {
            puntos += 2;
        }
        if (evento.getOrganizador().getNombre().toLowerCase(Locale.ROOT).contains(texto)) {
            puntos += 3;
        }
        if (nombres.stream().anyMatch(nombre -> nombre.toLowerCase(Locale.ROOT).contains(texto))) {
            puntos += 4;
        }

        return puntos;
    }

    private EventoResponseDTO detalle(Evento evento) {
        List<ParticipanteResponseDTO> participantes = eventoParticipanteRepository.findByEventoId(evento.getId())
                .stream()
                .map(vinculo -> participanteService.aParticipanteResponseDTO(vinculo.getParticipante()))
                .toList();

        return new EventoResponseDTO(
                evento.getId(),
                evento.getNombre(),
                evento.getDescripcion(),
                evento.getTipo(),
                evento.getEstado(),
                organizadorService.aOrganizadorResponseDTO(evento.getOrganizador()),
                participantes,
                evento.getFechaHora(),
                lugarService.aLugarResponseDTO(evento.getLugar()),
                evento.isAltaDemanda(),
                construirOfertas(evento),
                evento.getVersion());
    }

    // Devolvemos los sectores del evento con precios y asientos, no con stock
    // disponible.
    private List<PrecioSectorResponseDTO> construirOfertas(Evento evento) {
        List<Sector> sectoresDelLugar = sectorRepository.findByLugarIdOrderById(evento.getLugar().getId());
        if (sectoresDelLugar.isEmpty()) {
            return List.of();
        }

        Map<Long, BigDecimal> preciosPorSector = precioSectorRepository.findByEventoId(evento.getId())
                .stream()
                .collect(Collectors.toMap(
                        precio -> precio.getSector().getId(),
                        PrecioSector::getPrecio));

        List<Long> idsSectores = sectoresDelLugar.stream().map(Sector::getId).toList();
        Map<Long, List<AsientoResponseDTO>> asientosPorSector = new HashMap<>();

        for (Asiento asiento : asientoRepository.findBySectorIdIn(idsSectores)) {
            asientosPorSector.computeIfAbsent(
                    asiento.getSector().getId(),
                    clave -> new ArrayList<>()).add(asientoService.aAsientoResponseDTO(asiento));
        }

        for (List<AsientoResponseDTO> listaAsientos : asientosPorSector.values()) {
            listaAsientos.sort(
                    Comparator.comparing(AsientoResponseDTO::getFila)
                            .thenComparing(AsientoResponseDTO::getNumero));
        }

        return sectoresDelLugar.stream()
                .map(sector -> new PrecioSectorResponseDTO(
                        sector.getId(),
                        sector.getNombre(),
                        sector.isNumerado(),
                        sector.getCapacidad(),
                        preciosPorSector.get(sector.getId()),
                        asientosPorSector.getOrDefault(sector.getId(), List.of())))
                .toList();
    }

    private Evento buscarEvento(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado("Evento inexistente: " + id));
    }

    private void exigirEstado(Evento evento, EstadoEvento estadoEsperado, String mensaje) {
        if (evento.getEstado() != estadoEsperado) {
            throw ReglaNegocioException.conflicto(mensaje);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioSectorIndividualDTO obtenerPrecioSector(
            Long eventoId,
            Long sectorId) {

        // Verificamos que el evento exista
        buscarEvento(eventoId);

        PrecioSector precio = precioSectorRepository
                .findByEventoIdAndSectorId(eventoId, sectorId)
                .orElseThrow(() -> ReglaNegocioException.noEncontrado(
                        "El sector no tiene precio para este evento"));

        return new PrecioSectorIndividualDTO(
                eventoId,
                sectorId,
                precio.getPrecio());
    }

}
