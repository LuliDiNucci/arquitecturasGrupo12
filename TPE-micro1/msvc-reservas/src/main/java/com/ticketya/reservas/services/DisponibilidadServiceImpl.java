package com.ticketya.reservas.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.ticketya.reservas.clients.EventoClient;
import com.ticketya.reservas.dtos.AsientoDTO;
import com.ticketya.reservas.dtos.DisponibilidadDTO;
import com.ticketya.reservas.dtos.EventoDTO;
import com.ticketya.reservas.dtos.PrecioSectorDTO;
import com.ticketya.reservas.dtos.SectorDTO;
import com.ticketya.reservas.enums.EstadoReserva;
import com.ticketya.reservas.repositories.AsignacionAsientoRepository;
import com.ticketya.reservas.repositories.BloqueoEventoRepository;
import com.ticketya.reservas.repositories.DetalleReservaRepository;

@Service
public class DisponibilidadServiceImpl
        implements DisponibilidadService {

    private final EventoClient eventoClient;
    private final DetalleReservaRepository detalleRepo;
    private final AsignacionAsientoRepository asientoRepo;
    private final BloqueoEventoRepository bloqueoRepo;

    public DisponibilidadServiceImpl(
            EventoClient eventoClient,
            DetalleReservaRepository detalleRepo,
            AsignacionAsientoRepository asientoRepo,
            BloqueoEventoRepository bloqueoRepo) {

        this.eventoClient = eventoClient;
        this.detalleRepo = detalleRepo;
        this.asientoRepo = asientoRepo;
        this.bloqueoRepo = bloqueoRepo;
    }

    @Override
    public DisponibilidadDTO consultarDisponibilidad(
            Long eventoId, Long sectorId) {

        // 1. Obtener el evento y el sector
        if (eventoId == null || sectorId == null) {
            throw new IllegalArgumentException("Debe indicar eventoId y sectorId");
        }

        if (bloqueoRepo.findById(eventoId)
                .map(bloqueo -> bloqueo.isCancelado())
                .orElse(false)) {

            throw new IllegalStateException("El evento ya fue cancelado");
        }
        
        EventoDTO evento = eventoClient.obtenerEvento(eventoId);
        SectorDTO sector = eventoClient.obtenerSector(sectorId);

        // 2. Validar que existan
        if (eventoId == null || sectorId == null || evento == null || sector == null) {
            throw new IllegalArgumentException(
                    "El evento o sector no existe");
        }

        // 3. Validar que el sector pertenezca al lugar del evento
        if (evento.getLugarId() == null || sector.getLugarId() == null
                || !sector.getLugarId().equals(evento.getLugarId())) {
            throw new IllegalArgumentException(
                    "El sector no pertenece al lugar del evento");
        }

        if (sector.getNumerado() == null) {
            throw new IllegalArgumentException("Sector sin tipo definido");
        }
        PrecioSectorDTO precio = eventoClient.obtenerPrecioSector(eventoId, sectorId);
        if (precio == null || precio.getPrecio() == null ||
                !eventoId.equals(precio.getEventoId()) ||
                !sectorId.equals(precio.getSectorId())) {
            throw new IllegalArgumentException("Sector no configurado para este evento");
        }
        // 4. Crear el DTO de respuesta
        DisponibilidadDTO dto = new DisponibilidadDTO();
        dto.setEventoId(eventoId);
        dto.setSectorId(sectorId);

        LocalDateTime ahora = LocalDateTime.now();

        // 5. Distinguir el tipo de sector
        if (Boolean.TRUE.equals(sector.getNumerado())) {

            // SECTOR NUMERADO
            List<AsientoDTO> asientos = eventoClient.obtenerAsientosPorSector(sectorId);

            if (asientos == null) {
                throw new IllegalStateException("No se pudo obtener la lista de asientos");
            }
            List<Long> ocupados = asientoRepo.buscarAsientosOcupados(
                    eventoId,
                    sectorId,
                    EstadoReserva.CONFIRMADA,
                    EstadoReserva.ACTIVA,
                    ahora);

            Set<Long> idsOcupados = new HashSet<>(ocupados);
            List<Long> disponibles = new ArrayList<>();

            for (AsientoDTO asiento : asientos) {
                if (asiento != null && asiento.getId() != null
                        && sectorId.equals(asiento.getSectorId())
                        && !idsOcupados.contains(asiento.getId())) {
                    disponibles.add(asiento.getId());
                }
            }

            dto.setAsientosDisponibles(disponibles);
            dto.setLugaresDisponibles(disponibles.size());

        } else {

            // SECTOR NO NUMERADO
            Long ocupados = detalleRepo.contarLugaresOcupados(
                    eventoId,
                    sectorId,
                    EstadoReserva.CONFIRMADA,
                    EstadoReserva.ACTIVA,
                    ahora);

            if (sector.getCapacidad() == null || sector.getCapacidad() < 0) {
                throw new IllegalArgumentException("Capacidad del sector inválida");
            }
            int disponibles = sector.getCapacidad()
                    - ocupados.intValue();

            dto.setLugaresDisponibles(
                    Math.max(0, disponibles));
            dto.setAsientosDisponibles(new ArrayList<>());
        }

        return dto;
    }

}