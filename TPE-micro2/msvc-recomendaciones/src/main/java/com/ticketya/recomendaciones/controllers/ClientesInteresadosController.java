package com.ticketya.recomendaciones.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.recomendaciones.services.ClientesInteresadosService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/internal/clientes-interesados")
@Tag(name = "Clientes interesados", description = "Consulta interna de clientes interesados en un artista")
public class ClientesInteresadosController {

    private final ClientesInteresadosService service;

    public ClientesInteresadosController(
            ClientesInteresadosService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener clientes interesados", description = "Devuelve los IDs de clientes que tienen un artista favorito y coinciden con la ciudad del evento")
    public ResponseEntity<List<Long>> obtenerClientesInteresados(
            @RequestParam String nombreArtista,
            @RequestParam String ciudadEvento) {

        List<Long> clientes = service.buscarClientesInteresados(
                nombreArtista,
                ciudadEvento);

        return ResponseEntity.ok(clientes);
    }
}