
package com.ticketya.compras.dtos;

import java.math.BigDecimal;

public class ReporteSectorDTO {

    private Long sectorId;
    private Long cantidadVendida;
    private Long cantidadDevuelta;
    private BigDecimal importeBruto;
    private BigDecimal importeReintegrado;

    public ReporteSectorDTO() {
    }

    public ReporteSectorDTO(
            Long sectorId,
            Long cantidadVendida,
            Long cantidadDevuelta,
            BigDecimal importeBruto,
            BigDecimal importeReintegrado) {
        this.sectorId = sectorId;
        this.cantidadVendida = cantidadVendida;
        this.cantidadDevuelta = cantidadDevuelta;
        this.importeBruto = importeBruto;
        this.importeReintegrado = importeReintegrado;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public Long getCantidadVendida() {
        return cantidadVendida;
    }

    public Long getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public BigDecimal getImporteBruto() {
        return importeBruto;
    }

    public BigDecimal getImporteReintegrado() {
        return importeReintegrado;
    }
}