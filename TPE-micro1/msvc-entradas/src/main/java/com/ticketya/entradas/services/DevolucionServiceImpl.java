package com.ticketya.entradas.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketya.entradas.clients.CompraFeignClient;
import com.ticketya.entradas.clients.EventoFeignClient;
import com.ticketya.entradas.clients.ReservaFeignClient;
import com.ticketya.entradas.dtos.DevolverEntradaDTO;
import com.ticketya.entradas.dtos.DevolucionRequestDTO;
import com.ticketya.entradas.dtos.DevolucionResponseDTO;
import com.ticketya.entradas.dtos.EventoDTO;
import com.ticketya.entradas.entities.Entrada;
import com.ticketya.entradas.entities.SolicitudDevolucion;
import com.ticketya.entradas.repositories.EntradaRepository;
import com.ticketya.entradas.repositories.SolicitudDevolucionRepository;

@Service
public class DevolucionServiceImpl implements DevolucionService {

    private final SolicitudDevolucionRepository devolucionRepository;
    private final EntradaRepository entradaRepository;
    private final EventoFeignClient eventoFeignClient;
    private final ReservaFeignClient reservaFeignClient;
    private final CompraFeignClient compraFeignClient;

    public DevolucionServiceImpl(
            SolicitudDevolucionRepository devolucionRepository,
            EntradaRepository entradaRepository,
            EventoFeignClient eventoFeignClient,
            ReservaFeignClient reservaFeignClient,
            CompraFeignClient compraFeignClient) {

        this.devolucionRepository = devolucionRepository;
        this.entradaRepository = entradaRepository;
        this.eventoFeignClient = eventoFeignClient;
        this.reservaFeignClient = reservaFeignClient;
        this.compraFeignClient = compraFeignClient;
    }

    @Override
    @Transactional
    public DevolucionResponseDTO solicitar(DevolucionRequestDTO dto) {

        // Buscar la entrada.
        Entrada entrada = entradaRepository.findById(dto.getEntradaId())
                .orElseThrow(() ->
                    new RuntimeException("Entrada no encontrada"));

        // Verificar que la entrada esté emitida.
        if (!"EMITIDA".equals(entrada.getEstado())) {
            throw new RuntimeException(
                "La entrada no está disponible para devolución");
        }

        // Consultar el evento mediante Feign.
        EventoDTO evento =
                eventoFeignClient.obtenerEvento(entrada.getEventoId());

        // Verificar las 72 horas de anticipación.
        LocalDateTime limite = evento.fechaHora().minusHours(72);

        if (LocalDateTime.now().isAfter(limite)) {
            throw new RuntimeException(
                "Las devoluciones deben solicitarse al menos 72 horas antes del evento");
        }

        // Evitar solicitudes duplicadas pendientes.
        boolean pendiente = devolucionRepository
                .findByEntrada_IdAndEstado(entrada.getId(), "PENDIENTE")
                .isPresent();

        if (pendiente) {
            throw new RuntimeException(
                "Ya existe una solicitud pendiente para esta entrada");
        }

        SolicitudDevolucion solicitud = new SolicitudDevolucion();

        solicitud.setEntrada(entrada);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setMotivo(dto.getMotivo());
        solicitud.setEstado("PENDIENTE");

        SolicitudDevolucion guardada =
                devolucionRepository.save(solicitud);

        return convertirDTO(guardada);
    }

    @Override
    public List<DevolucionResponseDTO> obtenerPendientes() {

        return devolucionRepository.findByEstado("PENDIENTE")
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional
    public DevolucionResponseDTO aprobar(
            Long id, Long administradorId) {

        SolicitudDevolucion solicitud =
                devolucionRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException(
                "La solicitud ya fue resuelta");
        }

        Entrada entrada = solicitud.getEntrada();

        // Aprobar devolución.
        solicitud.setEstado("APROBADA");
        solicitud.setAdministradorId(administradorId);
        solicitud.setFechaResolucion(LocalDateTime.now());

        // Invalidar entrada.
        entrada.setEstado("INVALIDADA");

        entradaRepository.save(entrada);

        // Liberar únicamente el lugar correspondiente a esta entrada.
        reservaFeignClient.devolverEntrada(
                entrada.getReservaId(),
                new DevolverEntradaDTO(
                        entrada.getId(),
                        entrada.getSectorId(),
                        entrada.getAsientoId()));

        // Solicitar reintegro.
        compraFeignClient.reintegrarCompra(entrada.getCompraId());

        SolicitudDevolucion guardada =
                devolucionRepository.save(solicitud);

        return convertirDTO(guardada);
    }

    @Override
    @Transactional
    public DevolucionResponseDTO rechazar(
            Long id, Long administradorId) {

        SolicitudDevolucion solicitud =
                devolucionRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException(
                "La solicitud ya fue resuelta");
        }

        solicitud.setEstado("RECHAZADA");
        solicitud.setAdministradorId(administradorId);
        solicitud.setFechaResolucion(LocalDateTime.now());

        SolicitudDevolucion guardada =
                devolucionRepository.save(solicitud);

        return convertirDTO(guardada);
    }

    private DevolucionResponseDTO convertirDTO(
            SolicitudDevolucion solicitud) {

        return new DevolucionResponseDTO(
                solicitud.getId(),
                solicitud.getEntrada().getId(),
                solicitud.getFechaSolicitud(),
                solicitud.getMotivo(),
                solicitud.getEstado(),
                solicitud.getFechaResolucion(),
                solicitud.getAdministradorId()
        );
    }
}