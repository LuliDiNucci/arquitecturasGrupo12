
package com.ticketya.entradas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ticketya.entradas.dtos.DevolucionRequestDTO;

@FeignClient(name = "msvc-compras", url = "${msvc.compras.url}")
public interface CompraFeignClient {

    @PostMapping("/compras/reintegros")
    void reintegrarEntrada(
            @RequestBody DevolucionRequestDTO solicitud);
}
