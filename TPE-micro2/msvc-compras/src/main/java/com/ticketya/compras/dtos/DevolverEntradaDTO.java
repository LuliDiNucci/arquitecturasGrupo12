package com.ticketya.compras.dtos;

public class DevolverEntradaDTO {
    private Long entradaId;
    private Long sectorId;
    private Long asientoId;

    public DevolverEntradaDTO() {
    }

    public DevolverEntradaDTO(Long entradaId, Long sectorId, Long asientoId) {
        this.entradaId = entradaId;
        this.sectorId = sectorId;
        this.asientoId = asientoId;
    }

    public Long getEntradaId() { return entradaId; }
    public Long getSectorId() { return sectorId; }
    public Long getAsientoId() { return asientoId; }
}