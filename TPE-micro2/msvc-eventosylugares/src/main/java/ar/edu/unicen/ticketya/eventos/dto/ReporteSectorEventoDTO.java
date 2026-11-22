package ar.edu.unicen.ticketya.eventos.dto;

import java.math.BigDecimal;

public class ReporteSectorEventoDTO {
    private Long sectorId;
    private String nombreSector;
    private boolean numerado;
    private int capacidad;
    private long entradasVendidas;
    private long entradasDevueltas;
    private long entradasVigentes;
    private long lugaresOcupados;
    private BigDecimal porcentajeOcupacion;
    private BigDecimal importeBruto;
    private BigDecimal importeReintegrado;
    private BigDecimal totalRecaudado;

    public ReporteSectorEventoDTO() {}

    public ReporteSectorEventoDTO(Long sectorId, String nombreSector, boolean numerado, int capacidad,
            long entradasVendidas, long entradasDevueltas, long entradasVigentes, long lugaresOcupados,
            BigDecimal porcentajeOcupacion, BigDecimal importeBruto, BigDecimal importeReintegrado,
            BigDecimal totalRecaudado) {
        this.sectorId = sectorId;
        this.nombreSector = nombreSector;
        this.numerado = numerado;
        this.capacidad = capacidad;
        this.entradasVendidas = entradasVendidas;
        this.entradasDevueltas = entradasDevueltas;
        this.entradasVigentes = entradasVigentes;
        this.lugaresOcupados = lugaresOcupados;
        this.porcentajeOcupacion = porcentajeOcupacion;
        this.importeBruto = importeBruto;
        this.importeReintegrado = importeReintegrado;
        this.totalRecaudado = totalRecaudado;
    }

    public Long getSectorId() { return sectorId; }
    public String getNombreSector() { return nombreSector; }
    public boolean isNumerado() { return numerado; }
    public int getCapacidad() { return capacidad; }
    public long getEntradasVendidas() { return entradasVendidas; }
    public long getEntradasDevueltas() { return entradasDevueltas; }
    public long getEntradasVigentes() { return entradasVigentes; }
    public long getLugaresOcupados() { return lugaresOcupados; }
    public BigDecimal getPorcentajeOcupacion() { return porcentajeOcupacion; }
    public BigDecimal getImporteBruto() { return importeBruto; }
    public BigDecimal getImporteReintegrado() { return importeReintegrado; }
    public BigDecimal getTotalRecaudado() { return totalRecaudado; }
}
