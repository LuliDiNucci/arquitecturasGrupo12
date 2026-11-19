package com.ticketya.recomendaciones.exceptions;

public class CatalogoNoDisponibleException extends RuntimeException {
    public CatalogoNoDisponibleException(Throwable cause) {
        super("El catálogo de eventos no está disponible", cause);
    }
}
