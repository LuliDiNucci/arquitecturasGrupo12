package com.ticketya.notificaciones.services;

import com.ticketya.notificaciones.entities.EstadoNotificacion;
import com.ticketya.notificaciones.repositories.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.notificaciones.procesamiento-habilitado", matchIfMissing = true)
public class NotificadorPendientes {

    private static final Logger log = LoggerFactory.getLogger(NotificadorPendientes.class);
    private static final int TAMANIO_LOTE = 25;

    private final NotificacionRepository repository;
    private final ProcesadorNotificaciones procesador;
    private final Clock reloj;

    public NotificadorPendientes(NotificacionRepository repository, ProcesadorNotificaciones procesador,
                                 Clock reloj) {
        this.repository = repository;
        this.procesador = procesador;
        this.reloj = reloj;
    }

    @Scheduled(fixedDelayString = "${app.notificaciones.intervalo-ms}")
    public void procesarPendientes() {
        List<Long> pendientes = repository.buscarPendientes(EstadoNotificacion.PENDIENTE,
                reloj.instant(), PageRequest.ofSize(TAMANIO_LOTE));

        for (Long id : pendientes) {
            try {
                procesador.procesar(id);
            } catch (DataAccessException ex) {
                log.warn("Error de base de datos al procesar la notificación {}: {}",
                        id, ex.getClass().getSimpleName());
            }
        }
    }
}
