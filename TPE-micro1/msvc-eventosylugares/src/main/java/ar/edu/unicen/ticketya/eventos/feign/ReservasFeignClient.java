package ar.edu.unicen.ticketya.eventos.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

// Este cliente avisa una cancelacion a msvc-reservas.
// Ruta verificada en ReservaController. X-Operacion-Id es informativa para el receptor.
@FeignClient(name = "msvc-reservas", url = "${app.integraciones.reservas-base-url}")
public interface ReservasFeignClient {

    @PostMapping("/reservas/evento/{id}/cancelar")
    void cancelarEvento(@PathVariable("id") Long eventoId,
            @RequestHeader("X-Operacion-Id") String operacionId);

    @PostMapping("/reservas/evento/{id}/habilitar")
    void habilitarEvento(@PathVariable("id") Long eventoId);

}
