package com.ticketya.recomendaciones.exceptions;

public class PreferenciasExistentesException extends RuntimeException {

    public PreferenciasExistentesException(String mensaje) {
        super(mensaje);
    }
}