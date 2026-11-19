package com.ticketya.compras.services;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticketya.compras.clients.NotificacionClient;
import com.ticketya.compras.clients.UsuarioClient;
import com.ticketya.compras.dtos.CompraConfirmadaNotificacionDTO;
import com.ticketya.compras.dtos.DestinatarioNotificacionDTO;
import com.ticketya.compras.dtos.UsuarioContactoDTO;
import com.ticketya.compras.entities.AvisoCompraPendiente;
import com.ticketya.compras.repositories.AvisoCompraPendienteRepository;

@Component
public class DespachadorAvisosCompra {
    private static final Logger log = LoggerFactory.getLogger(DespachadorAvisosCompra.class);
    private final AvisoCompraPendienteRepository avisos;
    private final UsuarioClient usuarios;
    private final NotificacionClient notificaciones;
    private final Clock reloj;

    public DespachadorAvisosCompra(AvisoCompraPendienteRepository avisos, UsuarioClient usuarios,
                                  NotificacionClient notificaciones, Clock reloj) {
        this.avisos = avisos; this.usuarios = usuarios; this.notificaciones = notificaciones; this.reloj = reloj;
    }

    @Scheduled(fixedDelayString = "${notificaciones.compras.intervalo-ms:1000}")
    public void despacharPendientes() {
        for (AvisoCompraPendiente aviso : avisos.pendientes(reloj.instant(), PageRequest.ofSize(25))) {
            despachar(aviso.getId());
        }
    }

    @Transactional
    public void despachar(Long avisoId) {
        AvisoCompraPendiente aviso = avisos.findById(avisoId).orElse(null);
        if (aviso == null || !"PENDIENTE".equals(aviso.getEstado())
                || aviso.getProximoIntentoEn().isAfter(reloj.instant())) return;
        try (var correlation = com.ticketya.observability.Correlation.open(aviso.getCorrelationId())) {
            try {
                UsuarioContactoDTO contacto = usuarios.obtenerContacto(aviso.getClienteId());
                if (contacto == null || !aviso.getClienteId().equals(contacto.id())
                        || contacto.email() == null || contacto.email().isBlank()) {
                    throw new IllegalStateException("Usuarios no devolvió un email válido");
                }
                notificaciones.avisarCompraConfirmada(new CompraConfirmadaNotificacionDTO(
                        new DestinatarioNotificacionDTO(aviso.getClienteId(), contacto.email()),
                        aviso.getCompraId(), aviso.getNombreEvento()));
                aviso.entregado();
            } catch (RuntimeException ex) {
                aviso.reintentar(reloj.instant().plusSeconds(10));
                log.warn("No se pudo entregar el aviso de compra {}: {}", aviso.getCompraId(),
                        ex.getClass().getSimpleName());
            }
            avisos.save(aviso);
        }
    }
}
