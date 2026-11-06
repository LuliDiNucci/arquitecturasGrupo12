
package com.ticketya.compras.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketya.compras.dtos.CompraDTO;
import com.ticketya.compras.dtos.ConfirmacionCompraDTO;
import com.ticketya.compras.dtos.IniciarCompraDTO;
import com.ticketya.compras.services.CompraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Compras", description = "Gestión de compras de entradas a eventos")
@RestController
@RequestMapping("/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }


    @Operation(summary = "Iniciar una compra", description = "Crea una compra a partir de una reserva temporal. " +
            "Verifica que la reserva exista, pertenezca al cliente, " +
            "esté ACTIVA y no haya vencido. " +
            "Consulta los precios al microservicio Eventos y Lugares, " +
            "calcula el importe total y registra los detalles " +
            "y las entradas individuales. " +
            "La compra se crea en estado PENDIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compra creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
            @ApiResponse(responseCode = "404", description = "Reserva inexistente"),
            @ApiResponse(responseCode = "409", description = "Reserva vencida, no activa o con una compra asociada"),
            @ApiResponse(responseCode = "503", description = "Microservicio externo no disponible")
    })
    @PostMapping
    public ResponseEntity<CompraDTO> iniciarCompra(
            @RequestBody IniciarCompraDTO solicitud) {

        CompraDTO compra = compraService.iniciarCompra(solicitud);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(compra);
    }

    // CONSULTAR COMPRA
    @Operation(summary = "Consultar una compra por ID", description = "Obtiene los datos de una compra, " +
            "incluyendo su estado, importe total, " +
            "sectores, cantidades, precios y entradas individuales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compra encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Compra inexistente")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO> obtenerCompra(
            @PathVariable Long id) {

        CompraDTO compra = compraService.obtenerCompra(id);

        return ResponseEntity.ok(compra);
    }

    @Operation(
        summary = "Confirmar compra y emitir sus entradas",
        description = "Requiere pago aprobado; confirma Reservas y solicita a Entradas "
                    + "una emisión idempotente. Solo cambia a CONFIRMADA si se asocian "
                    + "todas las entradas. Puede repetirse tras una falla parcial."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Compra confirmada o ya confirmada"),
        @ApiResponse(responseCode = "400", description = "ID inválido"),
        @ApiResponse(responseCode = "404", description = "Compra o reserva inexistente"),
        @ApiResponse(responseCode = "409", description = "No hay pago aprobado, reserva vencida o emisión inválida"),
        @ApiResponse(responseCode = "503", description = "Reservas o Entradas no disponible")
    })
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<CompraDTO> confirmarCompra(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.confirmarCompra(id));
    }

    @Operation(summary = "Consultar confirmación de pago para emisión de entradas",
            description = "Devuelve el estado real de la compra y si existe un pago aprobado. "
                    + "CONFIRMACION_PENDIENTE significa que todavía no se emitieron todas las entradas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado de compra y pago"),
        @ApiResponse(responseCode = "404", description = "Compra no encontrada")
    })
    @GetMapping("/{id}/confirmacion")
    public ResponseEntity<ConfirmacionCompraDTO> consultarConfirmacion(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.consultarConfirmacion(id));
    }
}
