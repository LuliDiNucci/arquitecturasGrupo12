
package com.ticketya.reservas.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ticketya.reservas.clients.EventoClient;
import com.ticketya.reservas.dtos.AsientoDTO;
import com.ticketya.reservas.dtos.CrearReservaDTO;
import com.ticketya.reservas.dtos.DetalleReservaDTO;
import com.ticketya.reservas.dtos.DetalleReservaRespuestaDTO;
import com.ticketya.reservas.dtos.DevolverEntradaDTO;
import com.ticketya.reservas.dtos.EventoDTO;
import com.ticketya.reservas.dtos.PrecioSectorDTO;
import com.ticketya.reservas.dtos.ReservaDTO;
import com.ticketya.reservas.dtos.SectorDTO;
import com.ticketya.reservas.entities.AsignacionAsiento;
import com.ticketya.reservas.entities.BloqueoEvento;
import com.ticketya.reservas.entities.DetalleReserva;
import com.ticketya.reservas.entities.EntradaLiberada;
import com.ticketya.reservas.entities.Reserva;
import com.ticketya.reservas.enums.EstadoReserva;
import com.ticketya.reservas.repositories.AsignacionAsientoRepository;
import com.ticketya.reservas.repositories.BloqueoEventoRepository;
import com.ticketya.reservas.repositories.ClaveIdempotenciaBloqueoRepository;
import com.ticketya.reservas.repositories.DetalleReservaRepository;
import com.ticketya.reservas.repositories.EntradaLiberadaRepository;
import com.ticketya.reservas.repositories.ReservaRepository;

import feign.FeignException;

@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepo;
    private final DetalleReservaRepository detalleRepo;
    private final AsignacionAsientoRepository asientoRepo;
    private final BloqueoEventoRepository bloqueoRepo;
    private final EntradaLiberadaRepository entradaLiberadaRepo;
    private final EventoClient eventoClient;
    private final ClaveIdempotenciaBloqueoRepository claveBloqueoRepo;

    @Value("${reservas.max-entradas-por-cliente:4}")
    private int maxEntradasPorCliente;

    public ReservaServiceImpl(
            ReservaRepository reservaRepo,
            DetalleReservaRepository detalleRepo,
            AsignacionAsientoRepository asientoRepo,
            BloqueoEventoRepository bloqueoRepo,
            EventoClient eventoClient,
            EntradaLiberadaRepository entradaLiberadaRepo,
            ClaveIdempotenciaBloqueoRepository claveBloqueoRepo) {

        this.reservaRepo = reservaRepo;
        this.detalleRepo = detalleRepo;
        this.asientoRepo = asientoRepo;
        this.bloqueoRepo = bloqueoRepo;
        this.eventoClient = eventoClient;
        this.entradaLiberadaRepo = entradaLiberadaRepo;
        this.claveBloqueoRepo = claveBloqueoRepo;
    }

    // BLOQUEAR EL EVENTO

    private void bloquearEvento(Long eventoId) {

        bloqueoRepo.bloquear(eventoId)
                .orElseThrow(() -> new IllegalStateException(
                        "El evento todavía no está habilitado para reservas"));
    }

    // La clave se bloquea ANTES que el evento (orden fijo evita interbloqueos).
    private void bloquearClave(String clave) {
        claveBloqueoRepo.crearSiNoExiste(clave);
        claveBloqueoRepo.bloquear(clave)
                .orElseThrow(() -> new IllegalStateException("No se pudo bloquear la clave"));
    }

    private void exigirEventoNoCancelado(Long eventoId) {
        if (bloqueoRepo.findById(eventoId).orElseThrow().isCancelado()) {
            throw new IllegalStateException("El evento ya fue cancelado");
        }
    }

    // VALIDAR SOLICITUD
    private void validarSolicitud(CrearReservaDTO solicitud) {

        if (solicitud == null
                || solicitud.getClienteId() == null
                || solicitud.getEventoId() == null
                || solicitud.getClaveIdempotencia() == null
                || solicitud.getClaveIdempotencia().isBlank()
                || solicitud.getClaveIdempotencia().length() > 160
                || solicitud.getDetalles() == null
                || solicitud.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "Solicitud de reserva incompleta");
        }

        Set<Long> sectores = new HashSet<>();

        for (DetalleReservaDTO detalle : solicitud.getDetalles()) {

            if (detalle == null
                    || detalle.getSectorId() == null
                    || detalle.getCantidad() == null
                    || detalle.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "Detalle de reserva inválido");
            }

            if (!sectores.add(detalle.getSectorId())) {
                throw new IllegalArgumentException(
                        "No se puede repetir un sector");
            }
        }
    }

    // VALIDAR EVENTO Y SECTORES
    private void validarEventoYSectores(CrearReservaDTO solicitud) {

        EventoDTO evento = eventoClient.obtenerEvento(solicitud.getEventoId());

        if (evento == null || evento.getId() == null) {
            throw new IllegalArgumentException(
                    "El evento no existe");
        }

        if (evento.getFechaHora() == null ||
                !evento.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("El evento ya comenzó o no tiene fecha válida");
        }

        if (!"PUBLICADO".equals(evento.getEstado())) {
            throw new IllegalArgumentException(
                    "El evento no está habilitado para ventas");
        }

        for (DetalleReservaDTO detalle : solicitud.getDetalles()) {

            SectorDTO sector = eventoClient.obtenerSector(detalle.getSectorId());

            if (sector == null || sector.getId() == null
                    || sector.getLugarId() == null
                    || evento.getLugarId() == null) {

                throw new IllegalArgumentException(
                        "Sector o lugar inválido");
            }

            if (!sector.getLugarId().equals(evento.getLugarId())) {
                throw new IllegalArgumentException(
                        "El sector no pertenece al lugar del evento");
            }

            PrecioSectorDTO precio = eventoClient.obtenerPrecioSector(
                    solicitud.getEventoId(),
                    detalle.getSectorId());

            if (precio == null || precio.getPrecio() == null
                    || !solicitud.getEventoId().equals(precio.getEventoId())
                    || !detalle.getSectorId().equals(precio.getSectorId())) {

                throw new IllegalArgumentException(
                        "Sector no configurado para el evento");
            }

            if (sector.getNumerado() == null) {
                throw new IllegalArgumentException(
                        "Tipo de sector no definido");
            }

            List<Long> ids = detalle.getAsientoIds();

            if (Boolean.TRUE.equals(sector.getNumerado())) {

                if (ids == null
                        || ids.size() != detalle.getCantidad()
                        || new HashSet<>(ids).size() != ids.size()) {

                    throw new IllegalArgumentException(
                            "Cantidad o asientos inválidos");
                }

                for (Long asientoId : ids) {

                    if (asientoId == null) {
                        throw new IllegalArgumentException(
                                "El asiento no puede ser nulo");
                    }

                    AsientoDTO asiento = eventoClient.obtenerAsiento(asientoId);

                    if (asiento == null
                            || !detalle.getSectorId().equals(asiento.getSectorId())) {

                        throw new IllegalArgumentException(
                                "El asiento no pertenece al sector");
                    }
                }

            } else {

                if (ids != null && !ids.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Un sector no numerado no tiene asientos");
                }

                if (sector.getCapacidad() == null
                        || sector.getCapacidad() <= 0) {

                    throw new IllegalArgumentException(
                            "Capacidad del sector inválida");
                }
            }
        }
    }

    // COMPROBAR IDEMPOTENCIA
    private boolean mismaSolicitud(
            Reserva reserva,
            CrearReservaDTO solicitud) {

        if (!Objects.equals(reserva.getClienteId(),
                solicitud.getClienteId())
                || !Objects.equals(reserva.getEventoId(),
                        solicitud.getEventoId())
                || reserva.getDetalles().size() != solicitud.getDetalles().size()) {

            return false;
        }

        Map<Long, DetalleReserva> guardados = reserva.getDetalles().stream()
                .collect(Collectors.toMap(
                        DetalleReserva::getSectorId,
                        d -> d));

        for (DetalleReservaDTO pedido : solicitud.getDetalles()) {

            DetalleReserva guardado = guardados.get(pedido.getSectorId());

            if (guardado == null
                    || !Objects.equals(guardado.getCantidad(),
                            pedido.getCantidad())) {
                return false;
            }

            Set<Long> asientosGuardados = guardado.getAsignaciones().stream()
                    .map(AsignacionAsiento::getAsientoId)
                    .collect(Collectors.toSet());

            Set<Long> asientosPedidos = pedido.getAsientoIds() == null
                    ? Collections.emptySet()
                    : new HashSet<>(pedido.getAsientoIds());

            if (!asientosGuardados.equals(asientosPedidos)) {
                return false;
            }
        }

        return true;
    }

    // VALIDAR DISPONIBILIDAD
    private void validarDisponibilidad(CrearReservaDTO solicitud) {

        LocalDateTime ahora = LocalDateTime.now();

        for (DetalleReservaDTO detalle : solicitud.getDetalles()) {

            SectorDTO sector = eventoClient.obtenerSector(detalle.getSectorId());

            if (Boolean.TRUE.equals(sector.getNumerado())) {

                List<Long> ocupados = asientoRepo.buscarAsientosOcupados(
                        solicitud.getEventoId(),
                        detalle.getSectorId(),
                        EstadoReserva.CONFIRMADA,
                        EstadoReserva.ACTIVA,
                        ahora);

                Set<Long> idsOcupados = new HashSet<>(ocupados);

                for (Long asientoId : detalle.getAsientoIds()) {

                    if (idsOcupados.contains(asientoId)) {
                        throw new IllegalStateException(
                                "El asiento " + asientoId
                                        + " no está disponible");
                    }
                }

            } else {

                Long ocupados = detalleRepo.contarLugaresOcupados(
                        solicitud.getEventoId(),
                        detalle.getSectorId(),
                        EstadoReserva.CONFIRMADA,
                        EstadoReserva.ACTIVA,
                        ahora);

                long disponibles = sector.getCapacidad() - ocupados;

                if (disponibles < detalle.getCantidad()) {
                    throw new IllegalStateException(
                            "No hay suficientes lugares disponibles");
                }
            }
        }
    }

    // CONVERTIR ENTITY A DTO
    private ReservaDTO convertirADTO(Reserva reserva) {

        ReservaDTO dto = new ReservaDTO();

        dto.setId(reserva.getId());
        dto.setClienteId(reserva.getClienteId());
        dto.setEventoId(reserva.getEventoId());
        dto.setFechaCreacion(reserva.getFechaCreacion());
        dto.setFechaVencimiento(reserva.getFechaVencimiento());
        dto.setEstado(reserva.getEstado());

        List<DetalleReservaRespuestaDTO> detalles = new ArrayList<>();

        for (DetalleReserva detalle : reserva.getDetalles()) {

            DetalleReservaRespuestaDTO detalleDTO = new DetalleReservaRespuestaDTO();

            detalleDTO.setId(detalle.getId());
            detalleDTO.setSectorId(detalle.getSectorId());
            detalleDTO.setCantidad(detalle.getCantidad());
            detalleDTO.setCantidadLiberada(detalle.getCantidadLiberada());

            List<Long> asientos = new ArrayList<>();

            for (AsignacionAsiento asignacion : detalle.getAsignaciones()) {

                if (!Boolean.TRUE.equals(asignacion.getLiberado())) {
                    asientos.add(asignacion.getAsientoId());
                }
            }

            detalleDTO.setAsientoIds(asientos);

            detalles.add(detalleDTO);
        }

        dto.setDetalles(detalles);

        return dto;
    }

    // ACTUALIZAR RESERVAS VENCIDAS
    private void actualizarVencimiento(Reserva reserva) {

        if (reserva.getEstado() == EstadoReserva.ACTIVA
                && !reserva.getFechaVencimiento()
                        .isAfter(LocalDateTime.now())) {

            reserva.setEstado(EstadoReserva.VENCIDA);
        }
    }

    // CREAR RESERVA
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaDTO crearReserva(CrearReservaDTO solicitud) {

        validarSolicitud(solicitud);
        // Primero bloquear la clave global y luego la fila del evento.
        bloquearClave(solicitud.getClaveIdempotencia());
        bloquearEvento(solicitud.getEventoId());

        Optional<Reserva> existente = reservaRepo.findByClaveIdempotencia(
                solicitud.getClaveIdempotencia());

        if (existente.isPresent()) {

            Reserva anterior = existente.get();

            if (!mismaSolicitud(anterior, solicitud)) {
                throw new IllegalArgumentException(
                        "Clave de idempotencia reutilizada "
                                + "con datos diferentes");
            }

            actualizarVencimiento(anterior);

            return convertirADTO(anterior);
        }

        exigirEventoNoCancelado(solicitud.getEventoId());
        // Consultar el estado actual de Eventos después de adquirir el bloqueo local.
        validarEventoYSectores(solicitud);
        LocalDateTime ahora = LocalDateTime.now();

        long cantidadSolicitada = solicitud.getDetalles()
                .stream()
                .mapToLong(DetalleReservaDTO::getCantidad)
                .sum();

        long cantidadActual = reservaRepo.contarEntradasDelCliente(
                solicitud.getClienteId(),
                solicitud.getEventoId(),
                EstadoReserva.CONFIRMADA,
                EstadoReserva.ACTIVA,
                ahora);

        if (cantidadActual + cantidadSolicitada > maxEntradasPorCliente) {

            throw new IllegalArgumentException(
                    "Se supera el límite de entradas por cliente");
        }

        validarDisponibilidad(solicitud);

        Reserva reserva = new Reserva();

        reserva.setClienteId(solicitud.getClienteId());
        reserva.setEventoId(solicitud.getEventoId());
        reserva.setClaveIdempotencia(
                solicitud.getClaveIdempotencia());
        reserva.setFechaCreacion(ahora);
        reserva.setFechaVencimiento(ahora.plusMinutes(10));
        reserva.setEstado(EstadoReserva.ACTIVA);

        for (DetalleReservaDTO pedido : solicitud.getDetalles()) {

            DetalleReserva detalle = new DetalleReserva();

            detalle.setSectorId(pedido.getSectorId());
            detalle.setCantidad(pedido.getCantidad());

            if (pedido.getAsientoIds() != null) {

                for (Long asientoId : pedido.getAsientoIds()) {

                    AsignacionAsiento asignacion = new AsignacionAsiento();

                    asignacion.setAsientoId(asientoId);

                    detalle.agregarAsignacion(asignacion);
                }
            }

            reserva.agregarDetalle(detalle);
        }

        Reserva guardada = reservaRepo.saveAndFlush(reserva);

        return convertirADTO(guardada);
    }

    // OBTENER RESERVA
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaDTO obtenerReserva(Long reservaId) {

        Long eventoId = reservaRepo.buscarEventoId(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva inexistente"));

        bloquearEvento(eventoId);

        Reserva reserva = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva inexistente"));

        actualizarVencimiento(reserva);

        return convertirADTO(reserva);
    }

    // CONFIRMAR RESERVA
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaDTO confirmarReserva(Long reservaId) {

        Long eventoId = reservaRepo.buscarEventoId(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva inexistente"));

        bloquearEvento(eventoId);

        Reserva reserva = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva inexistente"));

        actualizarVencimiento(reserva);

        if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            return convertirADTO(reserva);
        }

        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new IllegalStateException(
                    "La reserva no está activa");
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);

        reservaRepo.save(reserva);

        return convertirADTO(reserva);
    }

    // LIBERAR RESERVA
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaDTO liberarReserva(Long reservaId) {

        Long eventoId = reservaRepo.buscarEventoId(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva inexistente"));

        bloquearEvento(eventoId);

        Reserva reserva = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reserva inexistente"));

        actualizarVencimiento(reserva);

        if (reserva.getEstado() == EstadoReserva.LIBERADA
                || reserva.getEstado() == EstadoReserva.VENCIDA
                || reserva.getEstado() == EstadoReserva.CANCELADA) {

            return convertirADTO(reserva);
        }

        if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "No se puede liberar una reserva confirmada "
                            + "mediante esta operación");
        }

        reserva.setEstado(EstadoReserva.LIBERADA);

        reservaRepo.save(reserva);

        return convertirADTO(reserva);
    }

    // CANCELAR RESERVAS POR EVENTO
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelarReservasEvento(Long eventoId) {

        bloquearEvento(eventoId);
        // Bloqueo local persistente: una creación nueva no podrá entrar
        // aunque Eventos todavía no haya actualizado su estado remoto.
        BloqueoEvento bloqueo = bloqueoRepo.findById(eventoId).orElseThrow();
        bloqueo.setCancelado(true);

        List<Reserva> reservas = reservaRepo.findByEventoId(eventoId);

        for (Reserva reserva : reservas) {

            actualizarVencimiento(reserva);

            if (reserva.getEstado() == EstadoReserva.ACTIVA) {
                reserva.setEstado(EstadoReserva.CANCELADA);
            }
        }

        reservaRepo.saveAll(reservas);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaDTO devolverEntrada(
            Long reservaId,
            DevolverEntradaDTO solicitud) {

        // 1. Validar los datos recibidos
        if (reservaId == null
                || solicitud == null
                || solicitud.getEntradaId() == null
                || solicitud.getSectorId() == null) {

            throw new IllegalArgumentException(
                    "Datos de devolución inválidos");
        }

        // 2. Buscar el evento y bloquearlo
        Long eventoId = reservaRepo.buscarEventoId(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva inexistente"));

        bloquearEvento(eventoId);

        // 3. Obtener la reserva
        Reserva reserva = reservaRepo.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva inexistente"));

        // 4. Verificar si esa entrada ya fue liberada
        Optional<EntradaLiberada> existente = entradaLiberadaRepo.findById(
                solicitud.getEntradaId());

        if (existente.isPresent()) {

            EntradaLiberada anterior = existente.get();

            if (!anterior.getReservaId().equals(reservaId)
                    || !anterior.getSectorId()
                            .equals(solicitud.getSectorId())
                    || !Objects.equals(
                            anterior.getAsientoId(),
                            solicitud.getAsientoId())) {

                throw new IllegalStateException(
                        "La entrada ya fue liberada con otros datos");
            }

            // Es un reintento de la misma devolución.
            return convertirADTO(reserva);
        }

        // 5. Verificar que esté confirmada
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "Solo se pueden devolver entradas confirmadas");
        }

        // 6. Buscar el detalle correspondiente al sector
        DetalleReserva detalleEncontrado = null;

        for (DetalleReserva detalle : reserva.getDetalles()) {

            if (detalle.getSectorId()
                    .equals(solicitud.getSectorId())) {

                detalleEncontrado = detalle;
                break;
            }
        }

        if (detalleEncontrado == null) {
            throw new IllegalArgumentException(
                    "El sector no pertenece a la reserva");
        }

        DetalleReserva detalle = detalleEncontrado;

        // 7. Comprobar que queden lugares ocupados
        int restantes = detalle.getCantidad()
                - detalle.getCantidadLiberada();

        if (restantes <= 0) {
            throw new IllegalStateException(
                    "No quedan lugares para liberar");
        }

        // 8. Consultar el tipo de sector
        SectorDTO sector = eventoClient.obtenerSector(solicitud.getSectorId());

        if (sector == null || sector.getNumerado() == null) {
            throw new IllegalArgumentException(
                    "Sector inválido");
        }

        if (Boolean.TRUE.equals(sector.getNumerado())) {

            // Sector numerado: liberar el asiento indicado
            if (solicitud.getAsientoId() == null) {
                throw new IllegalArgumentException(
                        "Debe indicar el asiento");
            }

            AsignacionAsiento asignacionEncontrada = null;

            for (AsignacionAsiento asignacion : detalle.getAsignaciones()) {

                if (asignacion.getAsientoId()
                        .equals(solicitud.getAsientoId())) {

                    asignacionEncontrada = asignacion;
                    break;
                }
            }

            if (asignacionEncontrada == null) {
                throw new IllegalArgumentException(
                        "El asiento no pertenece a la reserva");
            }

            if (Boolean.TRUE.equals(
                    asignacionEncontrada.getLiberado())) {

                throw new IllegalStateException(
                        "El asiento ya fue liberado");
            }

            asignacionEncontrada.setLiberado(true);

        } else {

            // Sector no numerado: no hay asiento específico
            if (solicitud.getAsientoId() != null) {
                throw new IllegalArgumentException(
                        "El sector no numerado no tiene asientos");
            }
        }

        // 9. Aumentar en uno la cantidad liberada
        detalle.setCantidadLiberada(
                detalle.getCantidadLiberada() + 1);

        // 10. Registrar la entrada liberada
        EntradaLiberada entradaLiberada = new EntradaLiberada();

        entradaLiberada.setEntradaId(solicitud.getEntradaId());
        entradaLiberada.setReservaId(reservaId);
        entradaLiberada.setSectorId(solicitud.getSectorId());
        entradaLiberada.setAsientoId(solicitud.getAsientoId());

        entradaLiberadaRepo.saveAndFlush(entradaLiberada);

        return convertirADTO(reserva);
    }

    @Override
    @Transactional
    public void habilitarEvento(Long eventoId) {

        if (eventoId == null || eventoId <= 0) {
            throw new IllegalArgumentException(
                    "ID de evento inválido");
        }

        try {
            EventoDTO evento = eventoClient.obtenerEvento(eventoId);

            if (evento == null) {
                throw new IllegalArgumentException("El evento no existe");
            }
            if (!"PUBLICADO".equals(evento.getEstado())) {
                throw new IllegalStateException(
                        "El evento no está publicado");
            }

        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("El evento no existe");
        }

        bloqueoRepo.crearSiNoExiste(eventoId);

        BloqueoEvento bloqueo = bloqueoRepo.findById(eventoId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo habilitar el evento"));

        if (bloqueo.isCancelado()) {
            throw new IllegalStateException(
                    "No se puede habilitar un evento cancelado");
        }
    }

}
