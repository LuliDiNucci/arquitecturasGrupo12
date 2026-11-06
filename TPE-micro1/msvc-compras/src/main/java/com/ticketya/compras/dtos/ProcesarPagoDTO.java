
package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para procesar el pago de una compra")
public class ProcesarPagoDTO {

    @Schema(description = "Medio de pago seleccionado", example = "TARJETA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String medioPago;

    @Schema(description = "Clave única para evitar cobros duplicados ante reintentos", example = "PAGO-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String claveIdempotencia;

    public ProcesarPagoDTO() {
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public void setClaveIdempotencia(String claveIdempotencia) {
        this.claveIdempotencia = claveIdempotencia;
    }
}
