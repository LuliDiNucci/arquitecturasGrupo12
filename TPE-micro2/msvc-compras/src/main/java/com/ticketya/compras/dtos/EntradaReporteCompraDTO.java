
package com.ticketya.compras.dtos;

import java.math.BigDecimal;

public record EntradaReporteCompraDTO(
        Long entradaId,
        Long compraId,
        Long eventoId,
        Long sectorId,
        Long asientoId,
        BigDecimal precioUnitario,
        String estadoCompra,
        String estadoPago,
        BigDecimal importeReintegrado,
        String estadoReintegro
) {
}
