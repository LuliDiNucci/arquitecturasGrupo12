package ar.edu.unicen.ticketya.eventos.integration;

import ar.edu.unicen.ticketya.eventos.dto.UsuarioContactoDTO;
import ar.edu.unicen.ticketya.eventos.feign.ComprasFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.NotificacionesEventoFeignClient;
import ar.edu.unicen.ticketya.eventos.feign.UsuariosFeignClient;
import ar.edu.unicen.ticketya.eventos.model.AvisoEventoPendiente;
import ar.edu.unicen.ticketya.eventos.repository.AvisoEventoPendienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.integraciones.habilitadas", havingValue = "true")
public class DespachadorAvisosEvento {

    private static final Logger log = LoggerFactory.getLogger(DespachadorAvisosEvento.class);

    private final AvisoEventoPendienteRepository avisos;
    private final ComprasFeignClient compras;
    private final UsuariosFeignClient usuarios;
    private final NotificacionesEventoFeignClient notificaciones;
    private final TransactionTemplate transacciones;
    private final Clock reloj;

    public DespachadorAvisosEvento(AvisoEventoPendienteRepository avisos, ComprasFeignClient compras,
            UsuariosFeignClient usuarios, NotificacionesEventoFeignClient notificaciones,
            TransactionTemplate transacciones, Clock reloj) {
        this.avisos = avisos;
        this.compras = compras;
        this.usuarios = usuarios;
        this.notificaciones = notificaciones;
        this.transacciones = transacciones;
        this.reloj = reloj;
    }

    @Scheduled(fixedDelayString = "${app.integraciones.reintento-ms:30000}")
    public void despacharPendientes() {
        for (Long id : avisos.buscarPendientes(reloj.instant(), PageRequest.ofSize(25))) {
            transacciones.executeWithoutResult(estado -> avisos.buscarParaDespachar(id)
                    .filter(aviso -> aviso.estaPendiente(reloj.instant()))
                    .ifPresent(this::despachar));
        }
    }

    private void despachar(AvisoEventoPendiente aviso) {
        try (var correlation = com.ticketya.observability.Correlation.open(aviso.getCorrelationId())) {
            List<Long> compradores;
            try {
                compradores = compras.buscarCompradores(aviso.getEventoId(), aviso.isCancelacion());
            } catch (RuntimeException error) {
                aviso.reintentar(reloj.instant().plusSeconds(10));
                log.warn("No se pudo consultar compradores del evento {}: {}", aviso.getEventoId(),
                        error.getClass().getSimpleName());
                return;
            }

            boolean huboFallo = false;
            for (Long clienteId : compradores) {
                try {
                    enviarCliente(aviso, clienteId);
                } catch (RuntimeException error) {
                    huboFallo = true;
                    log.warn("Falló el aviso del evento {} para el cliente {}: {}", aviso.getEventoId(), clienteId,
                            error.getClass().getSimpleName());
                }
            }
            if (huboFallo) {
                aviso.reintentar(reloj.instant().plusSeconds(10));
            } else {
                aviso.marcarEntregado();
            }
        }
    }

    private void enviarCliente(AvisoEventoPendiente aviso, Long clienteId) {
        UsuarioContactoDTO contacto = usuarios.obtenerContacto(clienteId);
        if (contacto == null || !clienteId.equals(contacto.id()) || contacto.email() == null
                || contacto.email().isBlank()) {
            throw new IllegalStateException("Usuarios no devolvió un email válido");
        }
        var destinatario = new NotificacionesEventoFeignClient.Destinatario(clienteId, contacto.email());
        if (aviso.isCancelacion()) {
            notificaciones.avisarCancelacion(new NotificacionesEventoFeignClient.EventoCancelado(
                    destinatario, aviso.getEventoId(), aviso.getNombreEvento()));
        } else {
            notificaciones.avisarModificacion(new NotificacionesEventoFeignClient.EventoModificado(
                    destinatario, aviso.getEventoId(), aviso.getVersionEvento(),
                    aviso.getNombreEvento(), aviso.getDetalleCambios()));
        }
    }
}
