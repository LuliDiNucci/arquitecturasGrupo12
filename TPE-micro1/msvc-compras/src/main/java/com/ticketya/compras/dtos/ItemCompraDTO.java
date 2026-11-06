
package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Entrada individual incluida en una compra")
public class ItemCompraDTO {

    @Schema(description = "ID del item de compra", example = "1")
    private Long id;

    @Schema(description = "ID del asiento, si el sector es numerado", example = "25", nullable = true)
    private Long asientoId;

    @Schema(description = "ID de la entrada emitida después del pago", nullable = true)
    private Long entradaId;

    public ItemCompraDTO(Long id, Long asientoId, Long entradaId) {
        this.id = id;
        this.asientoId = asientoId;
        this.entradaId = entradaId;
    }

    public Long getId() {
        return id;
    }

    public Long getAsientoId() {
        return asientoId;
    }

    public Long getEntradaId() {
        return entradaId;
    }
}
