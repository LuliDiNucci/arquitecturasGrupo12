
package com.ticketya.compras.dtos;

import java.math.BigDecimal;

public interface ReporteVentasSectorProjection {

    Long getSectorId();

    Long getCantidadVendida();

    BigDecimal getImporteBruto();
}
