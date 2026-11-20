package ar.edu.unicen.ticketya.eventos.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReporteComprasDTO {
    private Long eventoId;
    private BigDecimal importePagado;
    private BigDecimal importeReintegrado;
    private BigDecimal recaudacionNeta;
    private List<ReporteSectorComprasDTO> sectores;

    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
    public BigDecimal getImportePagado() { return importePagado; }
    public void setImportePagado(BigDecimal importePagado) { this.importePagado = importePagado; }
    public BigDecimal getImporteReintegrado() { return importeReintegrado; }
    public void setImporteReintegrado(BigDecimal importeReintegrado) { this.importeReintegrado = importeReintegrado; }
    public BigDecimal getRecaudacionNeta() { return recaudacionNeta; }
    public void setRecaudacionNeta(BigDecimal recaudacionNeta) { this.recaudacionNeta = recaudacionNeta; }
    public List<ReporteSectorComprasDTO> getSectores() { return sectores; }
    public void setSectores(List<ReporteSectorComprasDTO> sectores) { this.sectores = sectores; }
}
