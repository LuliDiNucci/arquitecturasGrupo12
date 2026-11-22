package ar.edu.unicen.ticketya.eventos.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReporteEventoDTO {
    private Long eventoId;
    private String nombreEvento;
    private long capacidadTotal;
    private long lugaresOcupados;
    private BigDecimal porcentajeOcupacion;
    private BigDecimal importePagado;
    private BigDecimal importeReintegrado;
    private BigDecimal recaudacionNeta;
    private List<ReporteSectorEventoDTO> sectores;

    public ReporteEventoDTO() {}

    public ReporteEventoDTO(Long eventoId, String nombreEvento, long capacidadTotal, long lugaresOcupados,
            BigDecimal porcentajeOcupacion, BigDecimal importePagado, BigDecimal importeReintegrado,
            BigDecimal recaudacionNeta, List<ReporteSectorEventoDTO> sectores) {
        this.eventoId = eventoId;
        this.nombreEvento = nombreEvento;
        this.capacidadTotal = capacidadTotal;
        this.lugaresOcupados = lugaresOcupados;
        this.porcentajeOcupacion = porcentajeOcupacion;
        this.importePagado = importePagado;
        this.importeReintegrado = importeReintegrado;
        this.recaudacionNeta = recaudacionNeta;
        this.sectores = sectores;
    }

    public Long getEventoId() { return eventoId; }
    public String getNombreEvento() { return nombreEvento; }
    public long getCapacidadTotal() { return capacidadTotal; }
    public long getLugaresOcupados() { return lugaresOcupados; }
    public BigDecimal getPorcentajeOcupacion() { return porcentajeOcupacion; }
    public BigDecimal getImportePagado() { return importePagado; }
    public BigDecimal getImporteReintegrado() { return importeReintegrado; }
    public BigDecimal getRecaudacionNeta() { return recaudacionNeta; }
    public List<ReporteSectorEventoDTO> getSectores() { return sectores; }
}
