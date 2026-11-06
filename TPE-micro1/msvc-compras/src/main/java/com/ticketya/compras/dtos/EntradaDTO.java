package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonAlias;

@Schema(description = "Entrada emitida o consultada en msvc-entradas (contrato propuesto)")
public class EntradaDTO {
    @Schema(description = "ID de la entrada", example = "100")
    private Long id;
    @Schema(description = "ID del item de Compra al que pertenece", example = "10")
    private Long itemCompraId;
    @Schema(description = "ID de la compra", example = "1")
    private Long compraId;
    @Schema(description = "Estado de la entrada", example = "EMITIDA")
    private String estado;
    @Schema(description = "Código QR único generado por Entradas", example = "QR-100-ABC")
    @JsonAlias("codigoQr")
    private String codigoQR;
    @Schema(description = "ID del sector de la entrada")
    private Long sectorId;
    @Schema(description = "ID del asiento, nulo si el sector no es numerado")
    private Long asientoId;
    @Schema(description = "Posición de la entrada en el detalle de reserva")
    private Integer indiceLugar;

    public EntradaDTO() { }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getItemCompraId() { return itemCompraId; }
    public void setItemCompraId(Long itemCompraId) { this.itemCompraId = itemCompraId; }
    public Long getCompraId() { return compraId; }
    public void setCompraId(Long compraId) { this.compraId = compraId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }
    public Long getAsientoId() { return asientoId; }
    public void setAsientoId(Long asientoId) { this.asientoId = asientoId; }
    public Integer getIndiceLugar() { return indiceLugar; }
    public void setIndiceLugar(Integer indiceLugar) { this.indiceLugar = indiceLugar; }
    public String getCodigoQR() { return codigoQR; }
    public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }
}
