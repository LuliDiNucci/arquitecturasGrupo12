package ar.edu.unicen.ticketya.eventos.integration;

import ar.edu.unicen.ticketya.eventos.feign.ComprasFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.EntradasFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.ReservasFeignClient;
import ar.edu.unicen.ticketya.eventos.model.NotificacionCancelacion;
import ar.edu.unicen.ticketya.eventos.repository.NotificacionCancelacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class NotificadorCancelaciones {

    private static final Logger log = LoggerFactory.getLogger(NotificadorCancelaciones.class);

    private final NotificacionCancelacionRepository pendientes;
    private final ReservasFeignClient reservasClient;
    private final EntradasFeignClient entradasClient;
    private final ComprasFeignClient comprasClient;
    private final Clock clock;
    private final boolean integracionesHabilitadas;

    public NotificadorCancelaciones(NotificacionCancelacionRepository pendientes,
                                    ReservasFeignClient reservasClient,
                                    EntradasFeignClient entradasClient,
                                    ComprasFeignClient comprasClient,
                                    Clock clock,
                                    @Value("${app.integraciones.habilitadas:false}") boolean integracionesHabilitadas) {
        this.pendientes = pendientes;
        this.reservasClient = reservasClient;
        this.entradasClient = entradasClient;
        this.comprasClient = comprasClient;
        this.clock = clock;
        this.integracionesHabilitadas = integracionesHabilitadas;
    }

    // Las cancelaciones se guardan primero en nuestra base de datos.
    // Si las integraciones estan apagadas, los avisos quedan pendientes.
    @Scheduled(fixedDelayString = "${app.integraciones.reintento-ms:30000}")
    @Transactional
    public void enviarPendientes() {
        if (!integracionesHabilitadas) {
            return;
        }

        for (NotificacionCancelacion aviso : pendientes.pendientes()) {
            aviso.setIntentos(aviso.getIntentos() + 1);
            aviso.setUltimoIntentoEn(LocalDateTime.now(clock));
            aviso.setUltimoError(null);
            String operacionId = "cancelacion-evento-" + aviso.getEventoId();

            // Reservas debe liberar los lugares ocupados por reservas activas.
            if (!aviso.isReservasNotificadas()) {
                try {
                    reservasClient.cancelarEvento(aviso.getEventoId(), operacionId);
                    aviso.setReservasNotificadas(true);
                } catch (Exception e) {
                    registrarError(aviso, "Reservas", e);
                }
            }

            // Entradas debe invalidar las entradas emitidas.
            if (!aviso.isEntradasNotificadas()) {
                try {
                    entradasClient.cancelarEvento(aviso.getEventoId(), operacionId);
                    aviso.setEntradasNotificadas(true);
                } catch (Exception e) {
                    registrarError(aviso, "Entradas", e);
                }
            }

            // Compras se encarga del reintegro; Eventos solo envia el aviso.
            if (!aviso.isComprasNotificadas()) {
                try {
                    comprasClient.cancelarEvento(aviso.getEventoId(), operacionId);
                    aviso.setComprasNotificadas(true);
                } catch (Exception e) {
                    registrarError(aviso, "Compras", e);
                }
            }

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
