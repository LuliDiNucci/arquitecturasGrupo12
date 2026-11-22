package ar.edu.unicen.ticketya.eventos.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ar.edu.unicen.ticketya.eventos.dto.ReporteComprasDTO;

@FeignClient(name = "msvc-compras", url = "${app.integraciones.compras-base-url}")
public interface ComprasFeignClient {

    @GetMapping("/compras/evento/{id}/compradores")
    List<Long> buscarCompradores(@PathVariable("id") Long eventoId,
            @RequestParam("incluirReintegrados") boolean incluirReintegrados);

    @PostMapping("/compras/evento/{id}/cancelar")
    void cancelarEvento(@PathVariable("id") Long eventoId,
                        @RequestHeader("X-Operacion-Id") String operacionId);

    @GetMapping("/compras/evento/{id}/reporte")
    ReporteComprasDTO obtenerReporte(@PathVariable("id") Long eventoId);
}
