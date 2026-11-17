package ar.edu.unicen.ticketya.eventos.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificacionesEvento", url = "${app.integraciones.notificaciones-base-url}")
public interface NotificacionesEventoFeignClient {

    record Destinatario(Long clienteId, String email) {}
    record EventoCancelado(Destinatario destinatario, Long eventoId, String nombreEvento) {}
    record EventoModificado(Destinatario destinatario, Long eventoId, Long versionEvento,
                            String nombreEvento, String detalleCambios) {}

    @PostMapping("/notificaciones/eventos-cancelados")
    void avisarCancelacion(@RequestBody EventoCancelado solicitud);

    @PostMapping("/notificaciones/eventos-modificados")
    void avisarModificacion(@RequestBody EventoModificado solicitud);
}
