package com.ticketya.msvc_fila_virtual.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

        @Operation(summary = "Ingresar a una fila virtual", description = "Registra al cliente autenticado en la fila de un evento "
                        + "publicado y de alta demanda. "
                        + "Si ya está registrado, devuelve el registro existente "
                        + "sin duplicarlo.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cliente registrado o previamente existente", content = @Content(schema = @Schema(implementation = ClienteEnFilaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
                        @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido"),
                        @ApiResponse(responseCode = "403", description = "No puede ingresar en nombre de otro cliente"),
                        @ApiResponse(responseCode = "404", description = "El evento no existe"),
                        @ApiResponse(responseCode = "409", description = "El evento no está publicado o no es de alta demanda"),
                        @ApiResponse(responseCode = "503", description = "Eventos y Lugares no está disponible")
        })
        @PostMapping("/{eventoId}/ingresar")
        public ResponseEntity<ClienteEnFilaDTO> ingresar(

                        @Parameter(description = "Identificador del evento", example = "1") @PathVariable @Positive Long eventoId,

                        @Valid @RequestBody IngresarFilaDTO solicitud,
                        @AuthenticationPrincipal Jwt jwt) {
                if (!solicitud.clienteId().toString().equals(jwt.getSubject())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No puede ingresar a la fila en nombre de otro cliente");
                }

                ClienteEnFilaDTO resultado = service.ingresar(
                                eventoId,
                                solicitud.clienteId());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(resultado);
        }

        @Operation(summary = "Consultar el estado de un cliente en la fila", description = "Obtiene el estado y la posición actual de un cliente. "
                        + "Un cliente puede consultar su propio registro. "
                        + "Los administradores también pueden consultar otros registros.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Estado del cliente consultado correctamente", content = @Content(schema = @Schema(implementation = ClienteEnFilaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Identificadores inválidos"),
                        @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido"),
                        @ApiResponse(responseCode = "403", description = "No tiene permisos para consultar el registro"),
                        @ApiResponse(responseCode = "404", description = "Cliente no registrado en la fila")
        })
        @GetMapping("/{eventoId}/clientes/{clienteId}")
        public ResponseEntity<ClienteEnFilaDTO> consultarEstado(

                        @Parameter(description = "Identificador del evento", example = "1") @PathVariable Long eventoId,

                        @Parameter(description = "Identificador del cliente", example = "10") @PathVariable Long clienteId,
                        @AuthenticationPrincipal Jwt jwt) {
                boolean esPropietario = clienteId.toString().equals(jwt.getSubject());

                boolean esAdministrador = jwt.getClaimAsStringList("roles") != null
                                && jwt.getClaimAsStringList("roles")
                                                .contains("ADMINISTRADOR");

                if (!esPropietario && !esAdministrador) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No puede consultar la fila de otro cliente");
                }

                return ResponseEntity.ok(
                                service.consultarEstado(eventoId, clienteId));
        }

        @Operation(summary = "Habilitar una tanda de clientes", description = "Permite a un administrador habilitar a los primeros "
                        + "clientes en espera, respetando el orden de ingreso. "
                        + "Cada autorización tiene una vigencia de 10 minutos. "
                        + "No se permite habilitar otra tanda mientras existan "
                        + "turnos habilitados y vigentes.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Clientes habilitados correctamente", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClienteEnFilaDTO.class)))),
                        @ApiResponse(responseCode = "400", description = "Evento o cantidad inválidos"),
                        @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido"),
                        @ApiResponse(responseCode = "403", description = "Se requiere el rol ADMINISTRADOR"),
                        @ApiResponse(responseCode = "409", description = "Existe una tanda activa o no quedan clientes esperando")
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

        @Operation(summary = "Validar un turno de fila virtual", description = "Operación interna utilizada por Reservas para verificar "
                        + "si un cliente tiene un turno habilitado y vigente. "
                        + "La validación no consume la autorización. "
                        + "Requiere credenciales de servicio de Reservas.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Validación realizada correctamente", content = @Content(schema = @Schema(implementation = ValidacionTurnoDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Identificadores inválidos"),
                        @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido"),
                        @ApiResponse(responseCode = "403", description = "Se requiere la identidad interna de Reservas"),
                        @ApiResponse(responseCode = "404", description = "Cliente no registrado en la fila")
        })
        @GetMapping("/{eventoId}/clientes/{clienteId}/validar")
        public ResponseEntity<ValidacionTurnoDTO> validarTurno(

                        @Parameter(description = "ID del evento", example = "1") @PathVariable Long eventoId,

                        @Parameter(description = "ID del cliente", example = "10") @PathVariable Long clienteId) {

                return ResponseEntity.ok(
                                service.validarTurno(eventoId, clienteId));
        }

        @Operation(summary = "Finalizar un turno de fila virtual", description = "Operación interna utilizada por Reservas para cerrar "
                        + "una autorización después de crear correctamente una reserva temporal. "
                        + "Permite finalizar turnos HABILITADOS y turnos VENCIDOS "
                        + "cuando se procesa una finalización asíncrona tardía. "
                        + "No permite finalizar turnos EN_ESPERA. "
                        + "La operación es idempotente y requiere credenciales "
                        + "de servicio de Reservas.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Turno finalizado o previamente finalizado", content = @Content(schema = @Schema(implementation = ClienteEnFilaDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Identificadores inválidos"),
                        @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido"),
                        @ApiResponse(responseCode = "403", description = "Se requiere la identidad interna de Reservas"),
                        @ApiResponse(responseCode = "404", description = "Cliente no registrado en la fila"),
                        @ApiResponse(responseCode = "409", description = "El turno se encuentra en un estado incompatible")
        })
        @PostMapping("/{eventoId}/clientes/{clienteId}/finalizar")
        public ResponseEntity<ClienteEnFilaDTO> finalizarTurno(

                        @Parameter(description = "ID del evento", example = "1") @PathVariable Long eventoId,

                        @Parameter(description = "ID del cliente", example = "10") @PathVariable Long clienteId) {

                return ResponseEntity.ok(
                                service.finalizarTurno(eventoId, clienteId));
        }

}