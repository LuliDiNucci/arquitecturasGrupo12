
package com.ticketya.compras.dtos;

import java.math.BigDecimal;
import java.util.List;

public class ReporteComprasEventoDTO {

    private Long eventoId;
    private BigDecimal importePagado;
    private BigDecimal importeReintegrado;
    private BigDecimal recaudacionNeta;
    private List<ReporteSectorDTO> sectores;

    public ReporteComprasEventoDTO() {
    }

    public ReporteComprasEventoDTO(
            Long eventoId,
            BigDecimal importePagado,
            BigDecimal importeReintegrado,
            BigDecimal recaudacionNeta,
            List<ReporteSectorDTO> sectores) {
        this.eventoId = eventoId;
        this.importePagado = importePagado;
        this.importeReintegrado = importeReintegrado;
        this.recaudacionNeta = recaudacionNeta;
        this.sectores = sectores;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public BigDecimal getImportePagado() {
        return importePagado;
    }

    public BigDecimal getImporteReintegrado() {
        return importeReintegrado;
    }

    public BigDecimal getRecaudacionNeta() {
        return recaudacionNeta;
    }

    public List<ReporteSectorDTO> getSectores() {
        return sectores;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public void setImportePagado(BigDecimal importePagado) {
        this.importePagado = importePagado;
    }

    public void setImporteReintegrado(BigDecimal importeReintegrado) {
        this.importeReintegrado = importeReintegrado;
    }

    public void setRecaudacionNeta(BigDecimal recaudacionNeta) {
        this.recaudacionNeta = recaudacionNeta;
    }

    public void setSectores(List<ReporteSectorDTO> sectores) {
        this.sectores = sectores;
    }
}
