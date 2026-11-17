package ar.edu.unicen.ticketya.eventos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PrecioSectorRequestDTO {
    @NotNull
    @Schema(example = "1")
    private Long sectorId;
    @NotNull @DecimalMin("0.00") @Digits(integer=10, fraction=2)
    @Schema(example = "35000.00")
    private BigDecimal precio;

    public PrecioSectorRequestDTO() {
    }

    public PrecioSectorRequestDTO(Long sectorId, BigDecimal precio) {
        this.sectorId = sectorId;
        this.precio = precio;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
