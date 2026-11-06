package ar.edu.unicen.ticketya.eventos.integration;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unicen.ticketya.eventos.feign.ComprasFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.EntradasFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.ReservasFeignClient;
import ar.edu.unicen.ticketya.eventos.model.NotificacionCancelacion;
import ar.edu.unicen.ticketya.eventos.repository.NotificacionCancelacionRepository;

@Service
public class NotificadorCancelaciones {

    private static final Logger log = LoggerFactory.getLogger(NotificadorCancelaciones.class);

    private final NotificacionCancelacionRepository pendientes;
    private final ReservasFeignClient reservasClient;
    private final EntradasFeignClient entradasClient;
    private final ComprasFeignClient comprasClient;
    private final Clock clock;
    private final boolean integracionesHabilitadas;
    private final boolean entradasHabilitadas;
    private final boolean comprasHabilitadas;

    public NotificadorCancelaciones(NotificacionCancelacionRepository pendientes,
                                    ReservasFeignClient reservasClient,
                                    EntradasFeignClient entradasClient,
                                    ComprasFeignClient comprasClient,
                                    Clock clock,
                                    @Value("${app.integraciones.habilitadas:false}") boolean integracionesHabilitadas,
                                    @Value("${app.integraciones.entradas.habilitadas:false}") boolean entradasHabilitadas,
                                    @Value("${app.integraciones.compras.habilitadas:false}") boolean comprasHabilitadas) {
        this.pendientes = pendientes;
        this.reservasClient = reservasClient;
        this.entradasClient = entradasClient;
        this.comprasClient = comprasClient;
        this.clock = clock;
        this.integracionesHabilitadas = integracionesHabilitadas;
        this.entradasHabilitadas = entradasHabilitadas;
        this.comprasHabilitadas = comprasHabilitadas;
    }

    // Las cancelaciones se guardan primero en nuestra base de datos.
    // Si las integraciones estan apagadas, los avisos quedan pendientes.

@Scheduled(fixedDelayString = "${app.integraciones.reintento-ms:30000}")
@Transactional
public void enviarPendientes() {

    // Si las integraciones estan deshabilitadas, no se envia nada
    if (!integracionesHabilitadas) {
        return;
    }

    // Recorrer las notificaciones de cancelacion pendientes
    for (NotificacionCancelacion aviso : pendientes.pendientes()) {

        // Verificar si queda algun servicio habilitado por notificar
        boolean hayNotificacionesHabilitadasPendientes =
                !aviso.isReservasNotificadas()
                || (entradasHabilitadas && !aviso.isEntradasNotificadas())
                || (comprasHabilitadas && !aviso.isComprasNotificadas());

        if (!hayNotificacionesHabilitadasPendientes) {
            continue;
        }

        // Registrar el intento de notificacion
        aviso.setIntentos(aviso.getIntentos() + 1);
        aviso.setUltimoIntentoEn(LocalDateTime.now(clock));
        aviso.setUltimoError(null);

        String operacionId = "cancelacion-evento-" + aviso.getEventoId();

        // NOTIFICAR A RESERVAS
        // Cancela las reservas activas y bloquea nuevas reservas
        if (!aviso.isReservasNotificadas()) {
            try {
                reservasClient.cancelarEvento(
                        aviso.getEventoId(),
                        operacionId
                );

                aviso.setReservasNotificadas(true);

            } catch (Exception e) {
                registrarError(aviso, "Reservas", e);
            }
        }

        // NOTIFICAR A ENTRADAS
        // Solo si la integracion esta habilitada
        if (entradasHabilitadas && !aviso.isEntradasNotificadas()) {
            try {
                entradasClient.cancelarEvento(
                        aviso.getEventoId(),
                        operacionId
                );

                aviso.setEntradasNotificadas(true);

            } catch (Exception e) {
                registrarError(aviso, "Entradas", e);
            }
        }

        // NOTIFICAR A COMPRAS
        // Solo si la integracion esta habilitada
        if (comprasHabilitadas && !aviso.isComprasNotificadas()) {
            try {
                comprasClient.cancelarEvento(
                        aviso.getEventoId(),
                        operacionId
                );

                aviso.setComprasNotificadas(true);

            } catch (Exception e) {
                registrarError(aviso, "Compras", e);
            }
        }

        // Guardar el estado de las notificaciones
        pendientes.save(aviso);
    }
}


    private void registrarError(NotificacionCancelacion aviso, String servicio, Exception e) {
        String mensaje = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        String resumen = mensaje.substring(0, Math.min(mensaje.length(), 350));
        String anterior = aviso.getUltimoError();
        aviso.setUltimoError((anterior == null ? "" : anterior + "; ") + servicio + ": " + resumen);
        log.warn("No se pudo avisar a {} sobre la cancelacion del evento {}",
                servicio, aviso.getEventoId(), e);
    }
}
