package com.ticketya.entradas.dtos;


public class DevolucionRequestDTO {
    private Long entradaId;
    private String motivo;

    public DevolucionRequestDTO() {}

    public DevolucionRequestDTO(Long entradaId, String motivo) {
        this.entradaId = entradaId;
        this.motivo = motivo;
    }

    public Long getEntradaId() { return entradaId; }
    public void setEntradaId(Long entradaId) { this.entradaId = entradaId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
