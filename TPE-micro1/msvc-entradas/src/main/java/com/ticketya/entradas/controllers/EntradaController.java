
package com.ticketya.entradas.controllers;

import java.util.List;

<<<<<<< HEAD
=======
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
>>>>>>> d3b986a48b26fce11bfe7325ed2a8e4e1b3645dd
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.entradas.dtos.EntradaRequestDTO;
import com.ticketya.entradas.dtos.EntradaResponseDTO;
import com.ticketya.entradas.services.EntradaService;

=======
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.entradas.dtos.EmitirEntradasRequestDTO;
import com.ticketya.entradas.dtos.EntradaDTO;
import com.ticketya.entradas.services.EntradaService;
import com.ticketya.entradas.dtos.ResultadoEmisionDTO;

import jakarta.validation.Valid;

@Validated
>>>>>>> d3b986a48b26fce11bfe7325ed2a8e4e1b3645dd
@RestController
@RequestMapping("/entradas")
public class EntradaController {

    private final EntradaService entradaService;

    public EntradaController(EntradaService entradaService) {
        this.entradaService = entradaService;
    }

<<<<<<< HEAD
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
=======
    /*
     * Emite las entradas correspondientes a una compra.
     * El service verifica que la compra este aprobada
     * y que la reserva se encuentre confirmada.
     *
     * Devuelve 201 si las entradas fueron creadas.
     * Devuelve 200 si ya estaban emitidas.
     */
    @PostMapping("/emitir")
    public ResponseEntity<List<EntradaDTO>> emitir(
         @Valid @RequestBody EmitirEntradasRequestDTO request) {

        ResultadoEmisionDTO resultado =
                entradaService.emitir(request.getCompraId());

        return ResponseEntity
                .status(
                    resultado.isCreada()
                        ? HttpStatus.CREATED
                        : HttpStatus.OK
                )
                .body(resultado.getEntradas());
    }

    /*
     * Obtiene una entrada por su ID.
     * Verifica que pertenezca al cliente indicado
     * y devuelve sus datos junto con el QR.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntradaDTO> obtener(
            @PathVariable Long id,
            @RequestHeader("X-Cliente-Id") Long clienteId) {

        EntradaDTO entrada =
                entradaService.obtener(id, clienteId);

        return ResponseEntity.ok(entrada);
    }

    /*
     * Obtiene todas las entradas de un cliente.
     * Devuelve una lista con los datos de cada entrada
     * y sus respectivos codigos QR.
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<EntradaDTO>> obtenerPorCliente(
            @PathVariable Long clienteId) {

        List<EntradaDTO> entradas =
                entradaService.obtenerPorCliente(clienteId);

        return ResponseEntity.ok(entradas);
    }
}
>>>>>>> d3b986a48b26fce11bfe7325ed2a8e4e1b3645dd
