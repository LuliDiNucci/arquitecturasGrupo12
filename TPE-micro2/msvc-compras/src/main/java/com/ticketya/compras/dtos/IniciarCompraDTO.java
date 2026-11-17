package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Solicitud para iniciar una compra a partir " +
                  "de una reserva temporal vigente"
)
public class IniciarCompraDTO {

    @Schema(
        description = "ID del cliente que inicia la compra. " +
                      "Debe coincidir con el cliente de la reserva.",
        example = "200",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long clienteId;

    @Schema(
        description = "ID de la reserva temporal que se desea pagar. " +
                      "Debe existir, estar ACTIVA y no haber vencido.",
        example = "9",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long reservaId;

    public IniciarCompraDTO() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }
}
