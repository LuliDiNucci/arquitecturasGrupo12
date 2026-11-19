package com.ticketya.recomendaciones.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ticketya.recomendaciones.config.CompraFeignConfig;

@FeignClient(name = "compraClient", url = "${app.compras.url}", configuration = CompraFeignConfig.class)
public interface CompraClient {

    @GetMapping("/compras/cliente/{clienteId}/eventos-recomendados")
    List<Long> obtenerEventosRecomendados(
            @PathVariable("clienteId") Long clienteId);
}