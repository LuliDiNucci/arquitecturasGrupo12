package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aviso de devolución de entrada aprobado por msvc-entradas")
public class SolicitudReintegroDTO {
    @Schema(description = "ID de entrada cuya devolución fue aprobada", example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long entradaId;
    @Schema(description = "Motivo informado por Entradas", example = "DEVOLUCION_CLIENTE")
    private String motivo;

    public SolicitudReintegroDTO() { }
    public Long getEntradaId() { return entradaId; }
    public void setEntradaId(Long entradaId) { this.entradaId = entradaId; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
