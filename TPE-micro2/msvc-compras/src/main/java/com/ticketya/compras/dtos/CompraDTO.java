
package com.ticketya.compras.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una compra y sus entradas")
public class CompraDTO {

    @Schema(description = "ID de la compra", example = "1")
    private Long id;

    @Schema(description = "ID del cliente", example = "200")
    private Long clienteId;

    @Schema(description = "ID de la reserva asociada", example = "9")
    private Long reservaId;

    @Schema(description = "ID del evento", example = "21")
    private Long eventoId;

    @Schema(description = "Fecha en que se inició la compra", example = "2026-11-05")
    private LocalDate fechaCompra;

    @Schema(description = "Importe total de la compra", example = "30000.00")
    private BigDecimal total;

    @Schema(description = "Estado de la compra", example = "PENDIENTE")
    private String estado;

    @Schema(description = "Detalle de sectores y entradas adquiridas")
    private List<DetalleCompraRespuestaDTO> detalles;

    public CompraDTO(
            Long id,
            Long clienteId,
            Long reservaId,
            Long eventoId,
            LocalDate fechaCompra,
            BigDecimal total,
            String estado,
            List<DetalleCompraRespuestaDTO> detalles) {

        this.id = id;
        this.clienteId = clienteId;
        this.reservaId = reservaId;
        this.eventoId = eventoId;
        this.fechaCompra = fechaCompra;
        this.total = total;
        this.estado = estado;
        this.detalles = detalles;
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    public List<DetalleCompraRespuestaDTO> getDetalles() {
        return detalles;
    }
}
