package com.ticketya.entradas.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.entradas.dtos.EntradaRequestDTO;
import com.ticketya.entradas.dtos.EntradaResponseDTO;
import com.ticketya.entradas.services.EntradaService;

@RestController
@RequestMapping("/entradas")
public class EntradaController {

    private final EntradaService entradaService;

    public EntradaController(EntradaService entradaService) {
        this.entradaService = entradaService;
    }

    // POST /entradas/emitir
    @PostMapping("/emitir")
    public EntradaResponseDTO emitir(
            @RequestBody EntradaRequestDTO dto) {

        return entradaService.emitir(dto);
    }

    // GET /entradas/{id}
    @GetMapping("/{id}")
    public EntradaResponseDTO obtenerPorId(
            @PathVariable Long id) {

        return entradaService.obtenerPorId(id);
    }

    // GET /entradas/cliente/{clienteId}
    @GetMapping("/cliente/{clienteId}")
    public List<EntradaResponseDTO> obtenerPorCliente(
            @PathVariable Long clienteId) {

        return entradaService.obtenerPorCliente(clienteId);
    }

    // POST /entradas/evento/{id}/invalidar
    @PostMapping("/evento/{id}/invalidar")
    public void invalidarPorEvento(
            @PathVariable Long id) {

        entradaService.invalidarPorEvento(id);
    }
}