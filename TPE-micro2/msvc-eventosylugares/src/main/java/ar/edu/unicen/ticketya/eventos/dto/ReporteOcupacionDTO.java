package ar.edu.unicen.ticketya.eventos.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReporteOcupacionDTO {
    private Long eventoId;
    private long capacidadTotal;
    private long lugaresOcupados;
    private BigDecimal porcentajeOcupacion;
    private List<ReporteSectorEventoDTO> sectores;

    public ReporteOcupacionDTO() {}

    public ReporteOcupacionDTO(Long eventoId, long capacidadTotal, long lugaresOcupados,
            BigDecimal porcentajeOcupacion, List<ReporteSectorEventoDTO> sectores) {
        this.eventoId = eventoId;
        this.capacidadTotal = capacidadTotal;
        this.lugaresOcupados = lugaresOcupados;
        this.porcentajeOcupacion = porcentajeOcupacion;
        this.sectores = sectores;
    }

    public Long getEventoId() { return eventoId; }
    public long getCapacidadTotal() { return capacidadTotal; }
    public long getLugaresOcupados() { return lugaresOcupados; }
    public BigDecimal getPorcentajeOcupacion() { return porcentajeOcupacion; }
    public List<ReporteSectorEventoDTO> getSectores() { return sectores; }
}
