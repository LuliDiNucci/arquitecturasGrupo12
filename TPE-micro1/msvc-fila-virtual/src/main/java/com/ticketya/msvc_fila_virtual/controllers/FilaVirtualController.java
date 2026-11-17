package com.ticketya.msvc_fila_virtual.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.msvc_fila_virtual.dtos.ClienteEnFilaDTO;
import com.ticketya.msvc_fila_virtual.dtos.HabilitarTandaDTO;
import com.ticketya.msvc_fila_virtual.dtos.IngresarFilaDTO;
import com.ticketya.msvc_fila_virtual.dtos.ValidacionTurnoDTO;
import com.ticketya.msvc_fila_virtual.services.FilaVirtualService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/filas")
@Tag(name = "Fila Virtual", description = "Gestión de clientes en filas virtuales para eventos de alta demanda")
public class FilaVirtualController {

    private final FilaVirtualService service;

    public FilaVirtualController(FilaVirtualService service) {
        this.service = service;
    }

    @Operation(summary = "Ingresar a una fila virtual", description = "Registra un cliente en la fila de un evento. "
            + "Si ya está registrado, devuelve el mismo registro sin duplicarlo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente registrado o previamente existente", content = @Content(schema = @Schema(implementation = ClienteEnFilaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflicto al registrar el cliente"),
            @ApiResponse(responseCode = "404", description = "El evento no existe"),
            @ApiResponse(responseCode = "503", description = "Eventos y Lugares no está disponible")
    })
    @PostMapping("/{eventoId}/ingresar")
    public ResponseEntity<ClienteEnFilaDTO> ingresar(

            @Parameter(description = "Identificador del evento", example = "1") @PathVariable @Positive Long eventoId,

            @Valid @RequestBody IngresarFilaDTO solicitud) {

        ClienteEnFilaDTO resultado = service.ingresar(
                eventoId,
                solicitud.clienteId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @Operation(summary = "Consultar el estado de un cliente en la fila", description = "Obtiene el estado y la posición actual de un cliente "
            + "registrado en la fila virtual de un evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del cliente consultado correctamente", content = @Content(schema = @Schema(implementation = ClienteEnFilaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Identificadores inválidos o cliente no registrado")
    })
    @GetMapping("/{eventoId}/clientes/{clienteId}")
    public ResponseEntity<ClienteEnFilaDTO> consultarEstado(

            @Parameter(description = "Identificador del evento", example = "1") @PathVariable Long eventoId,

            @Parameter(description = "Identificador del cliente", example = "10") @PathVariable Long clienteId) {

        return ResponseEntity.ok(
                service.consultarEstado(eventoId, clienteId));
    }

    @Operation(summary = "Habilitar una tanda de clientes", description = "Habilita a los primeros clientes en espera "
            + "respetando el orden de la fila. "
            + "Cada autorización dura 10 minutos. "
            + "No se permite habilitar otra tanda mientras "
            + "existan turnos activos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clientes habilitados correctamente", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClienteEnFilaDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Evento o cantidad inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una tanda activa "
                    + "o no quedan clientes esperando")
    })
    @PostMapping("/{eventoId}/habilitar")
    public ResponseEntity<List<ClienteEnFilaDTO>> habilitarTanda(

            @Parameter(description = "Identificador del evento", example = "1") @PathVariable Long eventoId,

            @Valid @RequestBody HabilitarTandaDTO solicitud) {

        return ResponseEntity.ok(
                service.habilitarTanda(
                        eventoId,
                        solicitud.cantidad()));
    }

    @Operation(summary = "Validar un turno de fila virtual", description = "Comprueba si un cliente está habilitado "
            + "y si su turno sigue vigente. "
            + "La validación no consume la autorización.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación realizada correctamente", content = @Content(schema = @Schema(implementation = ValidacionTurnoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Identificadores inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no registrado en la fila")
    })
    @GetMapping("/{eventoId}/clientes/{clienteId}/validar")
    public ResponseEntity<ValidacionTurnoDTO> validarTurno(

            @Parameter(description = "ID del evento", example = "1") @PathVariable Long eventoId,

            @Parameter(description = "ID del cliente", example = "10") @PathVariable Long clienteId) {

        return ResponseEntity.ok(
                service.validarTurno(eventoId, clienteId));
    }

    @Operation(summary = "Finalizar un turno de fila virtual", description = "Marca como FINALIZADO un turno habilitado "
            + "y vigente. Se utiliza después de confirmar "
            + "que la reserva fue creada correctamente. "
            + "La operación es idempotente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno finalizado correctamente", content = @Content(schema = @Schema(implementation = ClienteEnFilaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Identificadores inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no registrado en la fila"),
            @ApiResponse(responseCode = "409", description = "El turno no está habilitado o ya venció")
    })
    @PostMapping("/{eventoId}/clientes/{clienteId}/finalizar")
    public ResponseEntity<ClienteEnFilaDTO> finalizarTurno(

            @Parameter(description = "ID del evento", example = "1") @PathVariable Long eventoId,

            @Parameter(description = "ID del cliente", example = "10") @PathVariable Long clienteId) {

        return ResponseEntity.ok(
                service.finalizarTurno(eventoId, clienteId));
    }

}