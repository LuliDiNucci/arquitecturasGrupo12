package com.ticketya.usuarios.dto;

import jakarta.validation.constraints.NotNull;

public class EstadoRequest {
    @NotNull private Boolean activo;
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
