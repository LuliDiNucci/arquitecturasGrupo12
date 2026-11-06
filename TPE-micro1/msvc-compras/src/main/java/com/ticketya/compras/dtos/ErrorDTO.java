
package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error del microservicio Compras y Pagos")
public class ErrorDTO {

    @Schema(description = "Código de estado HTTP", example = "409")
    private int estado;

    @Schema(description = "Descripción del error ocurrido", example = "La reserva está vencida")
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
