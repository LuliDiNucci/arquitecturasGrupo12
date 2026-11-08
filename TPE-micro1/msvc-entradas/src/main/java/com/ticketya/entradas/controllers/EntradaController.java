
package com.ticketya.entradas.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.entradas.dtos.EmitirEntradasRequestDTO;
import com.ticketya.entradas.dtos.EntradaDTO;
import com.ticketya.entradas.dtos.ResultadoEmisionDTO;
import com.ticketya.entradas.services.EntradaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/entradas")
@Tag(name = "Entradas", description = "Operaciones para emitir, consultar e invalidar entradas")
public class EntradaController {

    private final EntradaService entradaService;

    public EntradaController(EntradaService entradaService) {
        this.entradaService = entradaService;
    }



   @Operation(summary = "Emitir entradas",description = "Emite las entradas correspondientes a una compra aprobada y con reserva confirmada. Si ya fueron emitidas, devuelve las existentes.")
   @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Entradas creadas correctamente",
            content = @Content(array = @ArraySchema(
                schema = @Schema(implementation = EntradaDTO.class)
            ))
        ),
        @ApiResponse(responseCode = "200", description = "Las entradas ya estaban emitidas"),
        @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
        @ApiResponse(responseCode = "409", description = "La compra o reserva no cumple las condiciones para emitir entradas")
    })
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



    
    @Operation(summary = "Obtener una entrada",description = "Obtiene los datos de una entrada y su código QR. Verifica que pertenezca al cliente indicado.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Entrada encontrada",
            content = @Content(schema = @Schema(implementation = EntradaDTO.class))
        ),
        @ApiResponse(responseCode = "403", description = "El cliente no es titular de la entrada"),
        @ApiResponse(responseCode = "404", description = "La entrada no existe")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntradaDTO> obtener(
            @PathVariable Long id,
            @RequestHeader("X-Cliente-Id") Long clienteId) {

        EntradaDTO entrada =
                entradaService.obtener(id, clienteId);

        return ResponseEntity.ok(entrada);
    }




    @Operation(summary = "Obtener entradas de un cliente",description = "Devuelve todas las entradas asociadas al identificador del cliente." )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado de entradas",
            content = @Content(array = @ArraySchema(
                schema = @Schema(implementation = EntradaDTO.class)
            ))
        )
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<EntradaDTO>> obtenerPorCliente(
            @PathVariable Long clienteId) {

        List<EntradaDTO> entradas =
                entradaService.obtenerPorCliente(clienteId);

        return ResponseEntity.ok(entradas);
    }




    @Operation(summary = "Invalidar entradas de un evento",description = "Invalida las entradas emitidas cuando el evento se encuentra cancelado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación de invalidación ejecutada"),
        @ApiResponse(responseCode = "500", description = "No se pudo completar la operación")
    })
    @PostMapping("/evento/{id}/invalidar")
    public void invalidarPorEvento(@PathVariable Long id) {
        entradaService.invalidarPorEvento(id);
    }
}
