package ar.edu.unicen.ticketya.eventos.dto;

import java.math.BigDecimal;

public class ReporteRecaudacionDTO {
    private Long eventoId;
    private BigDecimal importePagado;
    private BigDecimal importeReintegrado;
    private BigDecimal recaudacionNeta;

    public ReporteRecaudacionDTO() {}

    public ReporteRecaudacionDTO(Long eventoId, BigDecimal importePagado,
            BigDecimal importeReintegrado, BigDecimal recaudacionNeta) {
        this.eventoId = eventoId;
        this.importePagado = importePagado;
        this.importeReintegrado = importeReintegrado;
        this.recaudacionNeta = recaudacionNeta;
    }

    public Long getEventoId() { return eventoId; }
    public BigDecimal getImportePagado() { return importePagado; }
    public BigDecimal getImporteReintegrado() { return importeReintegrado; }
    public BigDecimal getRecaudacionNeta() { return recaudacionNeta; }
}
