package com.ticketya.reservas.services;

import java.time.Instant;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ticketya.reservas.entities.FinalizacionFilaPendiente;
import com.ticketya.reservas.repositories.FinalizacionFilaPendienteRepository;

@Component
public class DespachadorFinalizacionesFila {

    private final FinalizacionFilaPendienteRepository repository;
    private final ProcesadorFinalizacionesFila procesador;

    public DespachadorFinalizacionesFila(
            FinalizacionFilaPendienteRepository repository,
            ProcesadorFinalizacionesFila procesador) {

        this.repository = repository;
        this.procesador = procesador;
    }

    @Scheduled(fixedDelayString = "${fila-virtual.finalizaciones.intervalo-ms:5000}")
    public void procesarPendientes() {

        var pendientes = repository.findByEstadoAndProximoIntentoEnLessThanEqual(
                "PENDIENTE",
                Instant.now(),
                PageRequest.of(0, 25));

        for (FinalizacionFilaPendiente pendiente : pendientes) {

            procesador.procesar(pendiente.getId());
        }
    }
}