package com.ticketya.reservas.services;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketya.reservas.clients.FilaVirtualClient;
import com.ticketya.reservas.entities.FinalizacionFilaPendiente;
import com.ticketya.reservas.repositories.FinalizacionFilaPendienteRepository;

@Service
public class ProcesadorFinalizacionesFila {

    private static final Logger logger = LoggerFactory.getLogger(ProcesadorFinalizacionesFila.class);

    private final FinalizacionFilaPendienteRepository repository;
    private final FilaVirtualClient filaVirtualClient;

    @Value("${fila-virtual.finalizaciones.reintento-segundos:10}")
    private long segundosReintento;

    public ProcesadorFinalizacionesFila(
            FinalizacionFilaPendienteRepository repository,
            FilaVirtualClient filaVirtualClient) {

        this.repository = repository;
        this.filaVirtualClient = filaVirtualClient;
    }

    @Transactional
    public void procesar(Long finalizacionId) {

        FinalizacionFilaPendiente pendiente = repository
                .bloquearPorId(finalizacionId)
                .orElse(null);

        if (pendiente == null
                || !"PENDIENTE".equals(pendiente.getEstado())
                || pendiente.getProximoIntentoEn().isAfter(Instant.now())) {
            return;
        }

        try {

            filaVirtualClient.finalizarTurno(
                    pendiente.getEventoId(),
                    pendiente.getClienteId());

            pendiente.marcarFinalizada();

            logger.info(
                    "Turno finalizado para reserva {}",
                    pendiente.getReservaId());

        } catch (Exception ex) {

            pendiente.programarReintento(
                    Instant.now().plusSeconds(segundosReintento));

            logger.warn(
                    "No se pudo finalizar el turno de la reserva {}: {}",
                    pendiente.getReservaId(),
                    ex.getMessage());
        }

        repository.save(pendiente);
    }
}