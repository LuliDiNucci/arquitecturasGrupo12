package ar.edu.unicen.ticketya.eventos.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "recomendaciones", url = "${app.integraciones.recomendaciones-base-url}")
public interface RecomendacionesFeignClient {

        @GetMapping("/internal/clientes-interesados")
        List<Long> buscarClientesInteresados(
                        @RequestParam("nombreArtista") String nombreArtista,
                        @RequestParam("ciudadEvento") String ciudadEvento);
}