
package com.ticketya.compras.dtos;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de un sector incluido en una compra")
public class DetalleCompraRespuestaDTO {

    @Schema(description = "ID del detalle", example = "1")
    private Long id;

    @Schema(description = "ID del evento", example = "21")
    private Long eventoId;

    @Schema(description = "ID del sector", example = "5")
    private Long sectorId;

    @Schema(description = "Cantidad de entradas", example = "2")
    private Integer cantidad;

    @Schema(description = "Precio unitario al iniciar la compra", example = "15000.00")
    private BigDecimal precioUnitario;

    @Schema(description = "Subtotal del sector", example = "30000.00")
    private BigDecimal subtotal;

    @Schema(description = "Entradas individuales de este detalle")
    private List<ItemCompraDTO> items;

    public DetalleCompraRespuestaDTO(
            Long id,
            Long eventoId,
            Long sectorId,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal,
            List<ItemCompraDTO> items) {

        this.id = id;
        this.eventoId = eventoId;
        this.sectorId = sectorId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public List<ItemCompraDTO> getItems() {
        return items;
    }
}
