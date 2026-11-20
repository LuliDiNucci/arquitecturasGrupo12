
package com.ticketya.compras.dtos;

import java.math.BigDecimal;

public interface ReporteReintegrosSectorProjection {

    Long getSectorId();

    Long getCantidadDevuelta();

    BigDecimal getImporteReintegrado();
}
