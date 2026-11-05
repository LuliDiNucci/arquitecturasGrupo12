package ar.edu.unicen.ticketya.eventos.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/** Cliente preparado para el contrato propuesto; la ruta aun no existe en el receptor. */
@FeignClient(name = "msvc-compras", url = "${app.integraciones.compras-base-url}")
public interface ComprasFeignClient {

    @PostMapping("/compras/evento/{id}/cancelar")
    void cancelarEvento(@PathVariable("id") Long eventoId,
                        @RequestHeader("X-Operacion-Id") String operacionId);
}