package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de pago y confirmación de una compra, para el servicio de Entradas")
public class ConfirmacionCompraDTO {
    @Schema(example = "1") private Long id;
    @Schema(example = "200") private Long clienteId;
    @Schema(example = "10") private Long reservaId;
    @Schema(example = "CONFIRMACION_PENDIENTE") private String estado;
    @Schema(example = "true") private Boolean pagoAprobado;

    public ConfirmacionCompraDTO() {}
    public ConfirmacionCompraDTO(Long id, Long clienteId, Long reservaId, String estado, Boolean pagoAprobado) {
        this.id = id;
        this.clienteId = clienteId;
        this.reservaId = reservaId;
        this.estado = estado;
        this.pagoAprobado = pagoAprobado;
    }
    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Long getReservaId() { return reservaId; }
    public String getEstado() { return estado; }
    public Boolean getPagoAprobado() { return pagoAprobado; }
}
