package ar.edu.unicen.ticketya.eventos.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/** Cliente preparado para el contrato propuesto; la ruta aun no existe en el receptor. */
@FeignClient(name = "msvc-entradas", url = "${app.integraciones.entradas-base-url}")
public interface EntradasFeignClient {

    @PostMapping("/entradas/evento/{id}/invalidar")
    void cancelarEvento(@PathVariable("id") Long eventoId,
                        @RequestHeader("X-Operacion-Id") String operacionId);
}