package com.ticketya.reservas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.ticketya.reservas.dtos.ValidacionTurnoDTO;

@FeignClient(name = "filaVirtualClient", url = "${msvc.fila-virtual.url}")
public interface FilaVirtualClient {

    @GetMapping("/filas/{eventoId}/clientes/{clienteId}/validar")
    ValidacionTurnoDTO validarTurno(
            @PathVariable("eventoId") Long eventoId,
            @PathVariable("clienteId") Long clienteId);

    @PostMapping("/filas/{eventoId}/clientes/{clienteId}/finalizar")
    void finalizarTurno(
            @PathVariable("eventoId") Long eventoId,
            @PathVariable("clienteId") Long clienteId);
}