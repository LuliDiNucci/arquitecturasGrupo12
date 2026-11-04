
package com.ticketya.reservas.dtos;

public class ErrorDTO {

    private int estado;
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
