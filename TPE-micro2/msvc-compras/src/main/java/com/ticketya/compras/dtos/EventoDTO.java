
package com.ticketya.compras.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de un evento obtenida desde el microservicio Eventos")
public class EventoDTO {

    @Schema(description = "Identificador del evento", example = "21")
    private Long id;

    @Schema(description = "Estado actual del evento", example = "CANCELADO")
    private String estado;

    public EventoDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
