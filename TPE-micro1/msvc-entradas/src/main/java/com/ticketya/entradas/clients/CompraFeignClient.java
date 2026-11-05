package com.ticketya.entradas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "msvc-compras", url = "${msvc.compras.url}")
public interface CompraFeignClient {

    @PostMapping("/compras/{id}/reintegrar")
    void reintegrarCompra(@PathVariable("id") Long id);
}