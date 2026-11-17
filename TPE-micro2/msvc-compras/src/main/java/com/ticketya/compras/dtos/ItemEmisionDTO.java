package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lugar individual que requiere una entrada con QR único")
public class ItemEmisionDTO {
    @Schema(description = "ID del item de compra, único para asignar la entrada", example = "10")
    private Long itemCompraId;
    @Schema(description = "ID del sector", example = "5")
    private Long sectorId;
    @Schema(description = "ID del asiento o null para sectores no numerados", example = "25", nullable = true)
    private Long asientoId;

    public ItemEmisionDTO() { }
    public ItemEmisionDTO(Long itemCompraId, Long sectorId, Long asientoId) {
        this.itemCompraId = itemCompraId;
        this.sectorId = sectorId;
        this.asientoId = asientoId;
    }
    public Long getItemCompraId() { return itemCompraId; }
    public void setItemCompraId(Long itemCompraId) { this.itemCompraId = itemCompraId; }
    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }
    public Long getAsientoId() { return asientoId; }
    public void setAsientoId(Long asientoId) { this.asientoId = asientoId; }
}
