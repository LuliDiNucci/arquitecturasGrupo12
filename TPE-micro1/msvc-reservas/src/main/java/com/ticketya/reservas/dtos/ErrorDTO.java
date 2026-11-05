
package com.ticketya.reservas.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error de la API de Reservas")
public class ErrorDTO {

    @Schema(description = "Código HTTP del error", example = "409")
    private int estado;

    @Schema(description = "Mensaje que explica el motivo del error", example = "El evento ya fue cancelado")
    private String mensaje;

    public ErrorDTO() {
    }

    public ErrorDTO(int estado, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
