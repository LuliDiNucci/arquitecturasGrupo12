
package com.ticketya.reservas.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.reservas.dtos.CrearReservaDTO;
import com.ticketya.reservas.dtos.DevolverEntradaDTO;
import com.ticketya.reservas.dtos.ReservaDTO;
import com.ticketya.reservas.services.ReservaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reservas", description = "Creación, consulta, confirmación, liberación y cancelación de reservas")
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // CREAR RESERVA
    @Operation(summary = "Crear una reserva temporal", description = "Crea una reserva con duración de 10 minutos. " +
            "Valida disponibilidad, cantidad máxima de entradas, " +
            "asientos e idempotencia.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento, sector o asiento inexistente"),
            @ApiResponse(responseCode = "409", description = "Conflicto de disponibilidad"),
            @ApiResponse(responseCode = "503", description = "Servicio de Eventos no disponible")
    })
    @PostMapping
    public ResponseEntity<ReservaDTO> crearReserva(
            @RequestBody CrearReservaDTO solicitud) {

        ReservaDTO reserva = reservaService.crearReserva(solicitud);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reserva);
    }

    // OBTENER RESERVA
    @Operation(summary = "Consultar una reserva", description = "Obtiene los datos y el estado actual de una reserva por su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva inexistente")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> obtenerReserva(
            @PathVariable Long id) {

        ReservaDTO reserva = reservaService.obtenerReserva(id);

        return ResponseEntity.ok(reserva);
    }

    // CONFIRMAR RESERVA
    @Operation(summary = "Confirmar una reserva (interno)", description = "Operación invocada por Compras cuando se aprueba el pago. "
            +
            "Confirma una reserva activa y no vencida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva confirmada"),
            @ApiResponse(responseCode = "404", description = "Reserva inexistente"),
            @ApiResponse(responseCode = "409", description = "La reserva no puede confirmarse")
    })
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<ReservaDTO> confirmarReserva(
            @PathVariable Long id) {

        ReservaDTO reserva = reservaService.confirmarReserva(id);

        return ResponseEntity.ok(reserva);
    }

    // LIBERAR RESERVA
    @Operation(summary = "Liberar una reserva (interno)", description = "Libera una reserva temporal y sus lugares ocupados. "
            +
            "Se utiliza ante un pago rechazado o una liberación solicitada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva liberada"),
            @ApiResponse(responseCode = "404", description = "Reserva inexistente"),
            @ApiResponse(responseCode = "409", description = "Conflicto con el estado de la reserva")
    })
    @PostMapping("/{id}/liberar")
    public ResponseEntity<ReservaDTO> liberarReserva(
            @PathVariable Long id) {

        ReservaDTO reserva = reservaService.liberarReserva(id);

        return ResponseEntity.ok(reserva);
    }

    // CANCELAR RESERVAS POR EVENTO
    @Operation(summary = "Cancelar reservas de un evento (interno)", description = "Bloquea nuevas ventas y cancela las reservas activas del evento. "
            +
            "Las entradas ya confirmadas requieren coordinación con Compras y Entradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cancelación aplicada"),
            @ApiResponse(responseCode = "409", description = "Conflicto de estado")
    })
    @PostMapping("/evento/{eventoId}/cancelar")
    public ResponseEntity<Void> cancelarReservasEvento(
            @PathVariable Long eventoId) {

        reservaService.cancelarReservasEvento(eventoId);

        return ResponseEntity.noContent().build();
    }

    // DEVOLVER ENTRADA
    @Operation(summary = "Liberar una entrada devuelta (interno)", description = "Libera una entrada individual de una reserva confirmada. "
            +
            "La validación de titularidad, aprobación y plazo de devolución " +
            "debe realizarse previamente en los servicios correspondientes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada liberada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Reserva inexistente"),
            @ApiResponse(responseCode = "409", description = "La entrada no puede devolverse")
    })
    @PostMapping("/{id}/devolver-entrada")
    public ResponseEntity<ReservaDTO> devolverEntrada(
            @PathVariable Long id,
            @RequestBody DevolverEntradaDTO solicitud) {

        ReservaDTO reserva = reservaService.devolverEntrada(id, solicitud);

        return ResponseEntity.ok(reserva);
    }

    // HABILITAR EVENTO
    @Operation(summary = "Habilitar reservas para un evento (interno)", description = "Verifica que el evento exista, esté PUBLICADO "
            +
            "y no haya sido cancelado. Crea el registro de bloqueo " +
            "necesario para administrar las reservas concurrentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento habilitado"),
            @ApiResponse(responseCode = "400", description = "Evento inexistente o ID inválido"),
            @ApiResponse(responseCode = "409", description = "Evento no publicado o cancelado"),
            @ApiResponse(responseCode = "503", description = "Servicio de Eventos no disponible")
    })
    @PostMapping("/evento/{eventoId}/habilitar")
    public ResponseEntity<Void> habilitarEvento(
            @PathVariable Long eventoId) {

        reservaService.habilitarEvento(eventoId);

        return ResponseEntity.noContent().build();
    }
}

