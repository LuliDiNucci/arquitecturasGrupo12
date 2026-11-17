package com.ticketya.compras.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.compras.dtos.ReintegroDTO;
import com.ticketya.compras.dtos.SolicitudReintegroDTO;
import com.ticketya.compras.services.ReintegroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reintegros", description = "Reintegros por devolución de entradas o cancelación de eventos")
@RestController
@RequestMapping("/compras")
public class ReintegroController {

    private final ReintegroService reintegroService;

    public ReintegroController(ReintegroService reintegroService) {
        this.reintegroService = reintegroService;
    }

    @Operation(summary = "Reintegrar una entrada devuelta", description = "Comprueba en msvc-entradas que la devolución esté aprobada, "
            + "verifica pago aprobado y devuelve exactamente el precio histórico "
            + "de esa entrada. Es idempotente por entradaId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reintegro registrado o ya existente"),
            @ApiResponse(responseCode = "400", description = "ID de entrada inválido"),
            @ApiResponse(responseCode = "404", description = "Entrada no encontrada en Entradas"),
            @ApiResponse(responseCode = "409", description = "Entrada sin devolución aprobada o compra no confirmada"),
            @ApiResponse(responseCode = "503", description = "msvc-entradas no disponible")
    })
    @PostMapping("/reintegros")
    public ResponseEntity<ReintegroDTO> reintegrarEntrada(
            @RequestBody SolicitudReintegroDTO solicitud) {
        return ResponseEntity.ok(reintegroService.reintegrarEntrada(solicitud));
    }

    @Operation(summary = "Consultar reintegros de una compra", description = "Historial de reintegros individuales, con importe, estado y fechas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de reintegros"),
            @ApiResponse(responseCode = "404", description = "Compra inexistente")
    })
    @GetMapping("/{id}/reintegros")
    public ResponseEntity<List<ReintegroDTO>> obtenerReintegros(@PathVariable Long id) {
        return ResponseEntity.ok(reintegroService.obtenerReintegros(id));
    }

    @Operation(summary = "Notificar cancelación de evento", description = "Notificación interna de Eventos. Busca las compras pagadas y "
            + "reintegra cada entrada una sola vez. Los reintentos con otra "
            + "operación también se deduplican por entradaId. Incluye compras "
            + "cobradas sin entradas emitidas; verifica primero que los tickets existentes estén invalidados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reintegros de entradas emitidas procesados"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Existe una compra pagada aún sin entradas emitidas")
    })
    @PostMapping("/evento/{eventoId}/cancelar")
    public ResponseEntity<Void> cancelarEvento(
            @PathVariable Long eventoId,
            @RequestHeader("X-Operacion-Id") String operacionId) {
        reintegroService.reintegrarEventoCancelado(eventoId, operacionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reintentar la compensación de una compra", description = "Reintegra el importe de una compra cuyo "
            + "pago fue aprobado, pero cuya reserva no "
            + "pudo confirmarse. Utiliza los items originales "
            + "para evitar reintegros duplicados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compensación completada o ya realizada"),
            @ApiResponse(responseCode = "400", description = "ID de compra inválido"),
            @ApiResponse(responseCode = "404", description = "Compra inexistente"),
            @ApiResponse(responseCode = "409", description = "Compra no compensable o con entradas emitidas")
    })
    @PostMapping("/{id}/compensar")
    public ResponseEntity<Void> compensarCompra(
            @PathVariable Long id) {

        reintegroService.compensarCompraSinEntradas(id);

        return ResponseEntity.noContent().build();
    }
}
