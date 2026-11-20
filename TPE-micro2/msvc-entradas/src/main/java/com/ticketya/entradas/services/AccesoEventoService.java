package com.ticketya.entradas.services;

public interface AccesoEventoService {
    void asignar(Long eventoId, Long usuarioId);
    void revocar(Long eventoId, Long usuarioId);
    boolean puedeValidar(Long eventoId);
}
