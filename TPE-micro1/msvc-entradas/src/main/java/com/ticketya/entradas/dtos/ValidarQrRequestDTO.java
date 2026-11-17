package com.ticketya.entradas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Datos necesarios para validar una entrada mediante su código QR")
public class ValidarQrRequestDTO {

    @NotBlank
    @Schema(description = "Código QR de la entrada", example = "TICKET-ABC123")
    private String codigoQr;

    @NotNull
    @Positive
    @Schema(description = "Identificador del evento", example = "15")
    private Long eventoId;

    public String getCodigoQr() {
        return codigoQr;
    }

    public void setCodigoQr(String codigoQr) {
        this.codigoQr = codigoQr;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }
}