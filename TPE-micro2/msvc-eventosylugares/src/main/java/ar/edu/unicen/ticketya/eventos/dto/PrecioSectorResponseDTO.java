package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public class PrecioSectorResponseDTO {
    @Schema(example = "1")
    private Long sectorId;
    @Schema(example = "Platea")
    private String nombre;
    @Schema(example = "true")
    private boolean numerado;
    @Schema(example = "2")
    private int capacidad;
    @Schema(example = "35000.00")
    private BigDecimal precio;
    private List<AsientoResponseDTO> asientos;

    public PrecioSectorResponseDTO() {
    }

    public PrecioSectorResponseDTO(Long sectorId, String nombre, boolean numerado, int capacidad, BigDecimal precio, List<AsientoResponseDTO> asientos) {
        this.sectorId = sectorId;
        this.nombre = nombre;
        this.numerado = numerado;
        this.capacidad = capacidad;
        this.precio = precio;
        this.asientos = asientos;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isNumerado() {
        return numerado;
    }

    public void setNumerado(boolean numerado) {
        this.numerado = numerado;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public List<AsientoResponseDTO> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<AsientoResponseDTO> asientos) {
        this.asientos = asientos;
    }
}
