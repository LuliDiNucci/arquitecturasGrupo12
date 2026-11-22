package ar.edu.unicen.ticketya.eventos.dto;

import java.math.BigDecimal;

public class ReporteSectorComprasDTO {
    private Long sectorId;
    private Long cantidadVendida;
    private Long cantidadDevuelta;
    private BigDecimal importeBruto;
    private BigDecimal importeReintegrado;

    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }
    public Long getCantidadVendida() { return cantidadVendida; }
    public void setCantidadVendida(Long cantidadVendida) { this.cantidadVendida = cantidadVendida; }
    public Long getCantidadDevuelta() { return cantidadDevuelta; }
    public void setCantidadDevuelta(Long cantidadDevuelta) { this.cantidadDevuelta = cantidadDevuelta; }
    public BigDecimal getImporteBruto() { return importeBruto; }
    public void setImporteBruto(BigDecimal importeBruto) { this.importeBruto = importeBruto; }
    public BigDecimal getImporteReintegrado() { return importeReintegrado; }
    public void setImporteReintegrado(BigDecimal importeReintegrado) { this.importeReintegrado = importeReintegrado; }
}
