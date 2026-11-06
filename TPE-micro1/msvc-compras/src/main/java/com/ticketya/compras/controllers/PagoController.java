
package com.ticketya.compras.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.compras.dtos.PagoDTO;
import com.ticketya.compras.dtos.ProcesarPagoDTO;
import com.ticketya.compras.services.PagoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pagos", description = "Procesamiento de pagos e idempotencia")
@RestController
@RequestMapping("/compras")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @Operation(summary = "Procesar el pago de una compra", description = "Procesa un pago mediante una pasarela simulada. "
            +
            "Verifica que la compra esté PENDIENTE y que la " +
            "reserva continúe ACTIVA y vigente. " +
            "Registra el resultado APROBADO o RECHAZADO. " +
            "Utiliza una clave de idempotencia para evitar " +
            "cobros duplicados ante solicitudes repetidas. " +
            "Si el pago es aprobado, la compra pasa a " +
            "CONFIRMACION_PENDIENTE hasta completar la " +
            "confirmación de reserva y emisión de entradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado del pago registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud o medio de pago inválido"),
            @ApiResponse(responseCode = "404", description = "Compra o reserva inexistente"),
            @ApiResponse(responseCode = "409", description = "Compra no pendiente, reserva vencida " +
                    "o conflicto de idempotencia"),
            @ApiResponse(responseCode = "503", description = "Microservicio Reservas no disponible")
    })
    @PostMapping("/{id}/pagar")
    public ResponseEntity<PagoDTO> procesarPago(

            @Parameter(description = "ID de la compra que se desea pagar", example = "1", required = true) @PathVariable Long id,

            @RequestBody ProcesarPagoDTO solicitud) {

        PagoDTO pago = pagoService.procesarPago(id, solicitud);

        return ResponseEntity.ok(pago);
    }

    @Operation(summary = "Reintentar la liberación de una reserva", description = "Reintenta liberar la reserva asociada a una "
            + "compra cuyo pago fue rechazado. "
            + "Si la reserva ya está liberada, "
            + "la operación no vuelve a liberarla. "
            + "Al finalizar correctamente, la compra "
            + "queda en estado RECHAZADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Liberación completada o ya realizada"),
            @ApiResponse(responseCode = "400", description = "ID de compra inválido"),
            @ApiResponse(responseCode = "404", description = "Compra o reserva inexistente"),
            @ApiResponse(responseCode = "409", description = "La compra no tiene una liberación pendiente"),
            @ApiResponse(responseCode = "503", description = "Microservicio Reservas no disponible")
    })
    @PostMapping("/{id}/reintentar-liberacion")
    public ResponseEntity<Void> reintentarLiberacion(
            @Parameter(description = "ID de la compra", example = "1", required = true) @PathVariable Long id) {

        pagoService.reintentarLiberacion(id);

        return ResponseEntity.noContent().build();
    }

}
