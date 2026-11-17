package com.ticketya.compras.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.ticketya.compras.dtos.EntradaDTO;
import com.ticketya.compras.dtos.SolicitudEmisionDTO;


@FeignClient(name = "entradasClient", url = "${msvc.entradas.url}")
public interface EntradaClient {
    @PostMapping("/entradas/emitir")
    List<EntradaDTO> emitirEntradas(@RequestBody SolicitudEmisionDTO solicitud);

    @PostMapping("/entradas/evento/{eventoId}/invalidar")
    void invalidarPorEvento(@PathVariable("eventoId") Long eventoId);

    @GetMapping("/entradas/cliente/{clienteId}")
    List<EntradaDTO> obtenerPorCliente(@PathVariable("clienteId") Long clienteId);

    @GetMapping("/entradas/{id}")
    EntradaDTO obtenerEntrada(@PathVariable("id") Long entradaId,
            @RequestHeader("X-Cliente-Id") Long clienteId);
}
