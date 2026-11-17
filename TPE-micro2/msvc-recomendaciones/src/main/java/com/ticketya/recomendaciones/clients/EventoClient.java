

package com.ticketya.recomendaciones.clients;

import com.ticketya.recomendaciones.dtos.EventoResumenDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
    name = "msvc-eventos",
    url = "${app.eventos.url}"
)
public interface EventoClient {

    @GetMapping("/eventos/publicados")
    List<EventoResumenDTO> listarEventosPublicados();
}
