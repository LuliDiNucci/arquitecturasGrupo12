package com.ticketya.entradas.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ticketya.entradas.dtos.EventoDTO;

@FeignClient(name = "msvc-eventos-lugares", url = "${msvc.eventos.url}")
public interface EventoFeignClient {

    @GetMapping("/eventos/{id}")
    EventoDTO obtenerEvento(@PathVariable("id") Long id);
}